/*
Copyright (C) 2019-2021 Antmicro

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

SPDX-License-Identifier: Apache-2.0
*/

package DMAController

import scala.reflect.runtime.universe._
import DMAController.Bfm._
import DMAController.Bus._
import DMAController.Worker.{InterruptBundle, SyncBundle}
import chisel3.iotesters._
import chisel3._

class DMAFull(dut: DMATop) extends PeekPokeTester(dut){
  val width = 256
  val height = 256
  val min = 0
  val max = width * height * 2
  var cnt: Int = 0

  def waitRange(data: Bits, exp: Int, min: Int, max: Int) : Unit = {
    var cnt = 0

    while(peek(data) != exp && cnt < max){
      step(1)
      cnt += 1
    }

    assert(cnt < max)
    assert(cnt >= min)
  }

  val io = dut.io.asInstanceOf[Bundle{
                                val control: AXI4Lite
                                val read: AXIStream
                                val write: AXI4
                                val irq: InterruptBundle
                                val sync: SyncBundle}]

  val cls = runtimeMirror(getClass.getClassLoader).reflect(this)
  val members = cls.symbol.typeSignature.members

  def bfms = members.filter(_.typeSignature <:< typeOf[ChiselBfm])

  def stepSingle(): Unit = {
    for(bfm <- bfms){
      cls.reflectField(bfm.asTerm).get.asInstanceOf[ChiselBfm].update(cnt)
    }
    super.step(1)
  }

  override def step(n: Int): Unit = {
    for(_ <- 0 until n) {
      stepSingle
    }
  }

  val axil_master = new AxiLiteMasterBfm(io.control, peek, poke, println)
  val axis_master = new AxiStreamMasterBfm(io.read, width, peek, poke, println)
  val axi4_slave = new Axi4SlaveBfm(io.write, width * height, peek, poke, println)

  axis_master.loadFromFile("./img0.rgba")
  axi4_slave.loadFromFile("./img1.rgba")

  axil_master.writePush(0x10, 0)
  axil_master.writePush(0x14, width)
  axil_master.writePush(0x18, height)
  axil_master.writePush(0x1c, 0)

  axil_master.writePush(0x20, height * width * 4 + width * 2)
  axil_master.writePush(0x24, width)
  axil_master.writePush(0x28, height)
  axil_master.writePush(0x2c, width)

  step(100)

  axil_master.writePush(0x08, 3)

  axil_master.writePush(0x00, 0xf)

  waitRange(dut.io.irq.writerDone, 1, min, max)

  expect(dut.io.irq.writerDone, 1)
  expect(dut.io.irq.readerDone, 1)

  axi4_slave.saveToFile("./out.rgba")
}
