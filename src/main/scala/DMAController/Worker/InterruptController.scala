/*
Copyright (C) 2019-2022 Antmicro

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

SPDX-License-Identifier: Apache-2.0
*/

package DMAController.Worker

import DMAController.CSR.{CSRRegBundle, SetCSR, SimpleCSR}
import chisel3._
import chisel3.util.Cat

class InterruptController extends Module{
  val io = IO(new Bundle {
    val irq = new InterruptBundle
    val readBusy = Input(Bool())
    val writeBusy = Input(Bool())
    val imr = Flipped(new CSRRegBundle())
    val isr = Flipped(new CSRRegBundle())
  })

  val mask = WireInit(SimpleCSR(io.imr))

  val readBusy = RegNext(io.readBusy)
  val readBusyOld = RegNext(readBusy)

  val writeBusy = RegNext(io.writeBusy)
  val writeBusyOld = RegNext(writeBusy)

  val writeBusyIrq = RegInit(false.B)
  val readBusyIrq = RegInit(false.B)

  writeBusyIrq := writeBusyOld && !writeBusy && mask(0)
  readBusyIrq := readBusyOld && !readBusy && mask(1)

  val irq = WireInit(Cat(readBusyIrq, writeBusyIrq))

  val isr = WireInit(SetCSR(irq, io.isr))

  io.irq.writerDone := isr(0)
  io.irq.readerDone := isr(1)
}

object InterruptController {
  def apply(readBusy : Bool, writeBusy : Bool, imr : CSRRegBundle, isr : CSRRegBundle): InterruptBundle = {
    val irqc = Module(new InterruptController)

    irqc.io.readBusy := readBusy
    irqc.io.writeBusy := writeBusy

    irqc.io.imr <> imr
    irqc.io.isr <> isr

    irqc.io.irq
  }
}