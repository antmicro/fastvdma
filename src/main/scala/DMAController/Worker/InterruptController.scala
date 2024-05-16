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

package DMAController.Worker

import chisel3._
import chisel3.util.Cat
import DMAUtils.DMAModule
import DMAController.CSR._
import DMAController.DMAConfig.DMAConfig

class InterruptController(dmaConfig: DMAConfig) extends DMAModule(dmaConfig) {
  val io = IO(new Bundle {
    val irq = new InterruptBundle
    val readBusy = Input(Bool())
    val writeBusy = Input(Bool())
    val imr = Flipped(new CSRRegBundle(dmaConfig.controlDataWidth))
    val isr = Flipped(new CSRRegBundle(dmaConfig.controlDataWidth))
  })

  val mask = WireInit(SimpleCSR(io.imr, dmaConfig))

  val readBusy = RegNext(io.readBusy)
  val readBusyOld = RegNext(readBusy)

  val writeBusy = RegNext(io.writeBusy)
  val writeBusyOld = RegNext(writeBusy)

  val writeBusyIrq = RegInit(false.B)
  val readBusyIrq = RegInit(false.B)

  writeBusyIrq := writeBusyOld && !writeBusy && mask(0)
  readBusyIrq := readBusyOld && !readBusy && mask(1)

  val irq = WireInit(Cat(readBusyIrq, writeBusyIrq))

  val isr = WireInit(SetCSR(irq, io.isr, dmaConfig))

  io.irq.writerDone := isr(0)
  io.irq.readerDone := isr(1)
}

object InterruptController {
  def apply(readBusy: Bool, writeBusy: Bool, imr: CSRRegBundle, isr: CSRRegBundle,
            dmaConfig: DMAConfig): InterruptBundle = {
    val irqc = Module(new InterruptController(dmaConfig))

    irqc.io.readBusy := readBusy
    irqc.io.writeBusy := writeBusy

    irqc.io.imr <> imr
    irqc.io.isr <> isr

    irqc.io.irq
  }
}
