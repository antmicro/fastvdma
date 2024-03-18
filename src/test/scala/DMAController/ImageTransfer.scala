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

package DMAController

import DMAController.Bfm.ChiselBfm
import DMAController.Worker.{InterruptBundle, SyncBundle}
import chiseltest.iotesters.PeekPokeTester
import chisel3.Bits
import DMAController.DMAConfig._

class ImageTransfer(dut: DMATop, dmaFull: DMAFull, dmaConfig: DMAConfig) extends PeekPokeTester(dut){
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

  val reader = dmaFull.reader
  val writer = dmaFull.writer
  val control = dmaFull.control

  def stepSingle(): Unit = {
    reader.update(cnt)
    writer.update(cnt)
    control.update(cnt)
    super.step(1)
  }

  override def step(n: Int): Unit = {
    for(_ <- 0 until n) {
      stepSingle
    }
  }

  reader.loadFromFile("./img0.rgba")
  writer.loadFromFile("./img1.rgba")

  control.writePush(DMAConfig.Register.ReaderStartAddr, 0)
  control.writePush(DMAConfig.Register.ReaderLineLen, width)
  control.writePush(DMAConfig.Register.ReaderLineCnt, height)
  control.writePush(DMAConfig.Register.ReaderStride, 0)

  control.writePush(DMAConfig.Register.WriterStartAddr, height * width * 4 + width * 2)
  control.writePush(DMAConfig.Register.WriterLineLen, width)
  control.writePush(DMAConfig.Register.WriterLineCnt, height)
  control.writePush(DMAConfig.Register.WriterStride, width)

  step(100)

  control.writePush(DMAConfig.Register.InterruptMask, 3)

  control.writePush(DMAConfig.Register.Ctrl, 0xf)

  waitRange(dut.io.irq.writerDone, 1, min, max)

  expect(dut.io.irq.writerDone, 1)
  expect(dut.io.irq.readerDone, 1)

  writer.saveToFile(f"./out${dmaConfig.busConfig}.rgba")
}
