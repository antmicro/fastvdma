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

import chisel3._
import DMAController.Bus._
import DMAController.CSR.CSR
import DMAController.Frontend._
import DMAController.Worker.{InterruptBundle, WorkerCSRWrapper, SyncBundle}
import chisel3.util.Queue

class DMATop extends Module{
  val io = IO(new Bundle{
    val control = Flipped(new AXI4Lite(DMATop.controlAddrWidth, DMATop.controlDataWidth))
    val read = Flipped(new AXIStream(DMATop.readDataWidth))
    val write = new AXI4(DMATop.addrWidth, DMATop.writeDataWidth)
    val irq = new InterruptBundle
    val sync = new SyncBundle
  })

  val csrFrontend = Module(new AXI4LiteCSR(DMATop.addrWidth))

  val readerFrontend = Module(new AXIStreamSlave(DMATop.addrWidth, DMATop.readDataWidth))

  val writerFrontend = Module(new AXI4Writer(DMATop.addrWidth, DMATop.writeDataWidth))

  val csr = Module(new CSR(DMATop.addrWidth))

  val ctl = Module(new WorkerCSRWrapper(DMATop.addrWidth, DMATop.readDataWidth, DMATop.writeDataWidth,
    DMATop.readMaxBurst, DMATop.writeMaxBurst, DMATop.reader4KBarrier, DMATop.writer4KBarrier))

  val queue = Queue(readerFrontend.io.dataOut, DMATop.fifoDepth)
  queue <> writerFrontend.io.dataIn

  csrFrontend.io.ctl <> io.control
  csr.io.bus <> csrFrontend.io.bus
  ctl.io.csr <> csr.io.csr
  readerFrontend.io.xfer <> ctl.io.xferRead
  writerFrontend.io.xfer <> ctl.io.xferWrite

  io.read <> readerFrontend.io.bus
  io.write <> writerFrontend.io.bus

  io.irq <> ctl.io.irq
  io.sync <> ctl.io.sync

  assert(DMATop.readDataWidth == DMATop.writeDataWidth)

}

object DMATop {
  val addrWidth = 32
  val readDataWidth = 32
  val writeDataWidth = 32
  val readMaxBurst = 0
  val writeMaxBurst = 256
  val reader4KBarrier = false
  val writer4KBarrier = true

  val controlDataWidth = 32
  val controlAddrWidth = 32
  val controlRegCount = 16

  val fifoDepth = 512
}
