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

import scala.collection.mutable.ListBuffer

class AxiStreamMasterBfm(val axi: AXIStream,
                        val packetLen: Int,
                        val peek: Bits => BigInt,
                        val poke: (Bits, BigInt) => Unit,
                        val println: String => Unit) 
extends AxiStreamBfm {

  private var txList: ListBuffer[Int] = new ListBuffer()

  private object State extends Enumeration {
    type State = Value
    val Idle, WriteData= Value
  }

  private var state = State.Idle

  private var wordCnt: Int = 0

  private var tready: BigInt = 0

  def loadFromFile(filename: String): Unit = {
    val path = file.Paths.get(filename)
    val buffer = file.Files.readAllBytes(path)
    val bb = ByteBuffer.wrap(buffer)
    //bb.order(ByteOrder.nativeOrder)
    var buf = new Array[Int](buffer.length/4)
    bb.asIntBuffer.get(buf)
    for(i <- 0 until buf.length) {
      txList += buf(i)
    }
    println("AXI Stream BFM, file %s, %d words".format(filename, buf.length))
  }

  private def peekInputs(): Unit = {
    tready = peek(axi.tready)
  }

  private def putData(): Unit = {
    poke(axi.tdata, txList.remove(0))
  }

  private def updateTlast(): Unit = {
    if(wordCnt == packetLen - 1) {
      poke(axi.tlast, 1)
    } else {
      poke(axi.tlast, 0)
    }
  }
  
  def update(t: Long): Unit = {
    state match {
      case State.Idle => {
        if(txList.nonEmpty) {
          poke(axi.tvalid, 1)
          state = State.WriteData
          putData
          updateTlast
        }
      }
      case State.WriteData => {
        if(tready != 0) {
          if(txList.nonEmpty) {
            putData
            updateTlast
            if(wordCnt == packetLen) {
              wordCnt = 0
            } else {
              wordCnt += 1
            }
          } else {
            poke(axi.tvalid, 0)
            state = State.Idle
          }
        }
      }
    }
    peekInputs
  }
}
