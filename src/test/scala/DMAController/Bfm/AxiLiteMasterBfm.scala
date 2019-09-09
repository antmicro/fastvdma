/*
MIT License

Copyright (c) 2018 Jan Marjanovic
Copyright (c) 2019 Antmicro

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package DMAController.Bfm

import DMAController.Bus._
import chisel3.Bits

import scala.collection.mutable.ListBuffer

/**
  * Bus functional model for AXI Lite master
  *
  * Instantiation:
  *   provide connection to axi interface (`axi`) and utilities from Chisel
  *   PeekPoke tester
  *
  * Usage (read):
  *   call `readPush` with the desired address, then poll `getResponse`
  *   until you get the response with the data. Multiple commands can
  *   be "chained" one after another, just by calling `readPush` multiple
  *   times.
  *
  * Usage (write):
  *   call `writePush` with the desired address and data, then poll
  *   `getREsponse` until you get the response (with status code). As
  *   with reads commands, miltiple commands can be "chained" together
  *   just by callin `writePush` multiple times.
  *
  * Note:
  *   if both read and write commands are in the queue one after another,
  *   both actions will be performed in parallel.
  */
class AxiLiteMasterBfm(val axi: AXI4Lite,
                       val peek: Bits => BigInt,
                       val poke: (Bits, BigInt) => Unit,
                       val println: String => Unit)
    extends AxiLiteBfm {

  //==========================================================================
  // read interface
  class ReadIf {
    private var currCmd: Cmd = new Cmd(false, 0, 0)

    private object State extends Enumeration {
      type State = Value
      val Idle, ReadAddr, ReadData = Value
    }
    private var state = State.Idle

    private var ar_ready: BigInt = 0
    private var r_data: BigInt = 0
    private var r_resp: BigInt = 0
    private var r_valid: BigInt = 0

    private def peekInputs(): Unit = {
      ar_ready = peek(axi.ar.arready)
      r_data = peek(axi.r.rdata)
      r_resp = peek(axi.r.rresp)
      r_valid = peek(axi.r.rvalid)
    }

    private def printWithBg(s: String): Unit = {
      // black on cyan
      println("\u001b[30;46m" + s + "\u001b[39;49m")
    }

    def update(t: Long): Unit = {
      state match {
        case State.Idle => {
          if (cmdList.nonEmpty && cmdList(0).is_read) {
            currCmd = cmdList.remove(0)
            printWithBg(f"${t}%5d AxiLiteMasterBfm: read: leaving idle")
            state = State.ReadAddr
          }
        }
        case State.ReadAddr => {
          poke(axi.ar.arvalid, 1)
          poke(axi.ar.araddr, currCmd.addr)
          if (ar_ready != 0) {
            state = State.ReadData
          }
        }
        case State.ReadData => {
          poke(axi.ar.arvalid, 0)
          poke(axi.ar.araddr, 0)
          poke(axi.r.rready, 1)
          if (r_valid != 0) {
            printWithBg(f"${t}%5d AxiLiteMasterBfm: read: from addr 0x${currCmd.addr}%x data 0x${r_data}%08x")
            poke(axi.r.rready, 0)
            respList += new Resp(r_resp == 0, r_data)
            state = State.Idle
          }
        }
      }

      peekInputs
    }
  }

  //==========================================================================
  // write interface
  class WriteIf {
    private var currCmd: Cmd = new Cmd(false, 0, 0)

    private object State extends Enumeration {
      type State = Value
      val Idle, WriteAddr, WriteData, WriteWaitResp = Value
    }
    private var state = State.Idle

    private var aw_ready: BigInt = 0
    private var w_ready: BigInt = 0
    private var b_resp: BigInt = 0
    private var b_valid: BigInt = 0

    private def peekInputs(): Unit = {
      aw_ready = peek(axi.aw.awready)
      w_ready = peek(axi.w.wready)
      b_resp = peek(axi.b.bresp)
      b_valid = peek(axi.b.bvalid)
    }

    private def printWithBg(s: String): Unit = {
      // black on magenta
      println("\u001b[30;45m" + s + "\u001b[39;49m")
    }

    def update(t: Long): Unit = {
      state match {
        case State.Idle => {
          if (cmdList.nonEmpty && !cmdList(0).is_read) {
            currCmd = cmdList.remove(0)
            printWithBg(f"${t}%5d AxiLiteMasterBfm: write: leaving idle")
            state = State.WriteAddr
          }
        }
        case State.WriteAddr => {
          poke(axi.aw.awvalid, 1)
          poke(axi.aw.awaddr, currCmd.addr)
          if (aw_ready != 0) {
            state = State.WriteData
          }
        }
        case State.WriteData => {
          poke(axi.aw.awvalid, 0)
          poke(axi.aw.awaddr, 0)
          poke(axi.w.wvalid, 1)
          poke(axi.w.wdata, currCmd.wr_data)
          if (w_ready != 0) {
            state = State.WriteWaitResp
          }
        }
        case State.WriteWaitResp => {
          poke(axi.w.wvalid, 0)
          poke(axi.w.wdata, 0)
          poke(axi.b.bready, 1)
          if (b_valid != 0) {
            val resp_str = if (b_resp == 0) { "OK" } else { "ERR" }
            printWithBg(f"${t}%5d AxiLiteMasterBfm: write: from addr 0x${currCmd.addr}%x resp ${resp_str}")
            poke(axi.b.bready, 0)
            respList += new Resp(b_resp == 0, 0)
            state = State.Idle
          }
        }
      }

      peekInputs
    }
  }

  //==========================================================================
  // queues
  class Cmd(val is_read: Boolean, val addr: BigInt, val wr_data: BigInt)
  class Resp(val success: Boolean, val rd_data: BigInt)
  private var cmdList: ListBuffer[Cmd] = new ListBuffer()
  private var respList: ListBuffer[Resp] = new ListBuffer()

  // interfaces
  private val read_if = new ReadIf()
  private val write_if = new WriteIf()

  //==========================================================================
  // public functions

  /** return response if there is any */
  def getResponse(): Option[Resp] = {
    if (respList.isEmpty) {
      None
    } else {
      Some(respList.remove(0))
    }
  }

  /** push new read command into the command queue */
  def readPush(addr: BigInt): Unit = {
    cmdList += new Cmd(true, addr, 0)
  }

  /** push new write command into the command queue */
  def writePush(addr: BigInt, data: BigInt): Unit = {
    cmdList += new Cmd(false, addr, data)
  }

  /** part of ChiselBFM, should be called every clock cycle */
  def update(t: Long): Unit = {
    read_if.update(t)
    write_if.update(t)
  }
}
