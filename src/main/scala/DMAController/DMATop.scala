/*
Copyright (C) 2019-2025 Antmicro

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

class DMATop(implicit dmaConfig: DMAConfig) extends DMAModule {
  val io = IO(new Bundle {
    val control = Bus.getControlBus
    val read = Bus.getReaderBus
    val write = Bus.getWriterBus
    val irq = new InterruptBundle
    val sync = new SyncBundle
  })

  val csrFrontend = Module(Bus.getCSR)

  val readerFrontend = Module(Bus.getReader)

  val writerFrontend = Module(Bus.getWriter)

  val csr = Module(new CSR)

  val ctl = Module(new WorkerCSRWrapper)

  val queue = DMAQueue(readerFrontend.io.dataIO)
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