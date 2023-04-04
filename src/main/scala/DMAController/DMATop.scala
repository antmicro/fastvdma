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

import chisel3._
import chisel3.util._
import DMAController.Bus._
import DMAController.CSR._
import DMAController.Frontend._
import DMAController.Worker.{InterruptBundle, WorkerCSRWrapper, SyncBundle}
import DMAController.DMAConfig._
import DMAUtils._

class DMATop(dmaConfig: DMAConfig) extends DMAModule(dmaConfig) {
  val (reader, ccsr, writer) = dmaConfig.getBusConfig()
  val Bus = new Bus(dmaConfig)

  val io = IO(new Bundle{
    val control = Bus.getControlBus(ccsr)
    val read = Bus.getReaderBus(reader)
    val write = Bus.getWriterBus(writer)
    val irq = new InterruptBundle
    val sync = new SyncBundle
  })

  val csrFrontend = Module(Bus.getCSR(ccsr))

  val readerFrontend = Module(Bus.getReader(reader))

  val writerFrontend = Module(Bus.getWriter(writer))

  val csr = Module(new CSR(dmaConfig))

  val ctl = Module(new WorkerCSRWrapper(dmaConfig))

  val queue = Queue(readerFrontend.io.dataIO, dmaConfig.fifoDepth)
  queue <> writerFrontend.io.dataIO

  csrFrontend.io.ctl <> io.control
  csr.io.bus <> csrFrontend.io.bus
  ctl.io.csr <> csr.io.csr
  readerFrontend.io.xfer <> ctl.io.xferRead
  writerFrontend.io.xfer <> ctl.io.xferWrite

  io.read <> readerFrontend.io.bus
  io.write <> writerFrontend.io.bus

  io.irq <> ctl.io.irq
  io.sync <> ctl.io.sync

}