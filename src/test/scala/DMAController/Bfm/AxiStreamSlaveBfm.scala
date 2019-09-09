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

import scala.collection.mutable.ListBuffer

class AxiStreamSlaveBfm(val axi: AXIStream,
                        val peek: Bits => BigInt,
                        val poke: (Bits, BigInt) => Unit,
                        val println: String => Unit) 
extends AxiStreamBfm {

  private var rxList: ListBuffer[BigInt] = new ListBuffer()

  private object State extends Enumeration {
    type State = Value
    val Idle, ReadData = Value
  }

  private var state = State.Idle

  private var tvalid: BigInt = 0
  private var tdata: BigInt = 0
  private var tuser: BigInt = 0
  private var tlast: BigInt = 0

  private def peekInputs(): Unit = {
    tvalid = peek(axi.tvalid)
    tdata = peek(axi.tdata)
    tuser = peek(axi.tuser)
    tlast = peek(axi.tlast)
  }
  
  def update(t: Long): Unit = {
    state match {
      case State.Idle => {
        poke(axi.tready, 1)
        state = State.ReadData
      }
      case State.ReadData => {
        if(tvalid != 0) {
          rxList += tdata
        }
      }
    }
    peekInputs
  }
}
