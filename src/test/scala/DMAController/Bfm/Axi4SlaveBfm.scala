/*
Copyright (C) 2019-2023 Antmicro

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

SPDX-License-Identifier: Apache-2.0
*/

package DMAController.Bfm

import DMAController.Bus._
import chisel3.Bits

import java.nio._

class Axi4MemoryBfm(val axi: AXI4,
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
    private object State extends Enumeration {
      type State = Value
      val Idle, ReadAddr, ReadData = Value
    }

    private var state = State.Idle

    private var araddr: BigInt = 0
    private var arlen: BigInt = 0
    private var arvalid: BigInt = 0

    private var rready: BigInt = 0

    private var addr: Int = 0
    private var len: Int = 0
    private var xferLen: Int = 0

    private def peekInputs(): Unit = {
      araddr = peek(axi.ar.araddr)
      arlen = peek(axi.ar.arlen)
      arvalid = peek(axi.ar.arvalid)

      rready = peek(axi.r.rready)
    }

    def update(t: Long): Unit = {
      state match {
        case State.Idle => {
          poke(axi.ar.arready, 1)
          poke(axi.r.rlast, 0)
          poke(axi.r.rvalid, 0)
          state = State.ReadAddr
          len = 0
        }
        case State.ReadAddr => {
          if(arvalid != 0) {
            addr = araddr.toInt / 4
            xferLen = arlen.toInt + 1
            poke(axi.ar.arready, 0)
            state = State.ReadData
          }
        }
        case State.ReadData => {
          if(rready != 0) {
            poke(axi.r.rdata, buf(addr))
            addr += 1
            len += 1
            poke(axi.r.rvalid, 1)

            if(xferLen == len) {
              poke(axi.r.rlast, 1)
              state = State.Idle
            }
          }
        }
      }
      peekInputs
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
