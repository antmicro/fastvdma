/*
MIT License

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

import java.nio._

class Axi4SlaveBfm(val axi: AXI4,
                        val size: Int,
                        val peek: Bits => BigInt,
                        val poke: (Bits, BigInt) => Unit,
                        val println: String => Unit) 
extends Axi4Bfm {

  var buf: Array[Int] = new Array[Int](size)

  class Write {
    private object State extends Enumeration {
      type State = Value
      val Idle, WriteAddr, WriteData, WriteResp = Value
    }

    private var state = State.Idle

    private var awaddr: BigInt = 0
    private var awlen: BigInt = 0
    private var awvalid: BigInt = 0

    private var wdata: BigInt = 0
    private var wlast: BigInt = 0
    private var wvalid: BigInt = 0

    private var bready: BigInt = 0

    private var addr: Int = 0
    private var len: Int = 0
    private var xferLen: Int = 0

    private def peekInputs(): Unit = {
      awaddr = peek(axi.aw.awaddr)
      awlen = peek(axi.aw.awlen)
      awvalid = peek(axi.aw.awvalid)

      wdata = peek(axi.w.wdata)
      wlast = peek(axi.w.wlast)
      wvalid = peek(axi.w.wvalid)

      bready = peek(axi.b.bready)
    }

    def update(t: Long): Unit = {
      state match {
        case State.Idle => {
          poke(axi.aw.awready, 1)
          state = State.WriteAddr
          len = 0
        }
        case State.WriteAddr => {
          if(awvalid != 0) {
            addr = awaddr.toInt / 4
            xferLen = awlen.toInt + 1
            poke(axi.aw.awready, 0)
            poke(axi.w.wready, 1)
            state = State.WriteData
          }
        }
        case State.WriteData => {
          if(wvalid != 0) {
            buf(addr) = wdata.toInt
            addr += 1
            len += 1
            if(wlast != 0) {
              poke(axi.w.wready, 0)
              poke(axi.b.bvalid, 1)
              poke(axi.b.bresp, 0)
              state = State.WriteResp
              if(xferLen != len) {
                println("Transfer len doesn't match, %d %d".format(xferLen, len))
              }
            }
          }
        }
        case State.WriteResp => {
          if(bready != 0) {
            poke(axi.b.bvalid, 0)
            state = State.Idle
          }
        }
      }
      peekInputs
    }
  }

  class Read {
    def update(t: Long): Unit = {

    }
  }

  def loadFromFile(filename: String): Unit = {
    val path = file.Paths.get(filename)
    val buffer = file.Files.readAllBytes(path)
    val bb = ByteBuffer.wrap(buffer)
    buf = new Array[Int](buffer.length/4)
    bb.asIntBuffer.get(buf)
  }

  def saveToFile(filename: String): Unit = {
    val path = file.Paths.get(filename)
    val bb = ByteBuffer.allocate(4*buf.length)
    for (i <- 0 until buf.length) {
      bb.putInt(buf(i))
    }
    file.Files.write(path, bb.array())
  }

  private val writer = new Write()
  private val reader = new Read()

  def update(t: Long): Unit = {
    writer.update(t)
    reader.update(t)
  }
}
