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

import DMAController.CSR._
import DMAController.DMAConfig._
import DMAUtils._
import chisel3._
import chisel3.util.Cat

class WorkerCSRWrapper(cfg: DMAConfig) extends DMAModule(cfg) {
  val io = IO(new Bundle {
    val csr: Vec[CSRRegBundle] = Vec(cfg.controlRegCount, Flipped(new CSRRegBundle(cfg.controlDataWidth)))
    val irq = new InterruptBundle
    val sync = new SyncBundle
    val xferRead = new XferDescBundle(cfg.addrWidth)
    val xferWrite = new XferDescBundle(cfg.addrWidth)
  })

  val addressGeneratorRead = Module(new AddressGenerator(cfg.addrWidth, cfg.readDataWidth, cfg))
  val transferSplitterRead = Module(new TransferSplitter(cfg.addrWidth, cfg.readDataWidth,
                                                         cfg.readMaxBurst, cfg.reader4KBarrier, cfg))
  val addressGeneratorWrite = Module(new AddressGenerator(cfg.addrWidth, cfg.writeDataWidth, cfg))
  val transferSplitterWrite = Module(new TransferSplitter(cfg.addrWidth, cfg.writeDataWidth,
                                                          cfg.writeMaxBurst, cfg.writer4KBarrier, cfg))

  val status = RegNext(Cat(addressGeneratorRead.io.ctl.busy, addressGeneratorWrite.io.ctl.busy))

  val readerSync = RegNext(io.sync.readerSync)
  val readerSyncOld = RegNext(readerSync)

  val writerSync = RegNext(io.sync.writerSync)
  val writerSyncOld = RegNext(writerSync)

  val readerStart = RegInit(false.B)
  val writerStart = RegInit(false.B)

  val control = Wire(UInt())
  val clear = Wire(UInt())

  val tag = scala.sys.env.getOrElse("TAG", "v0.0")
  val version = RegInit(tag.filter(_.isDigit).toInt.U)
  val (in, csr, out) = cfg.getBusConfig()
  val encConfig = RegInit((in << 8 | csr << 4 | out).U(cfg.addrWidth.W))

  control := ClearCSR(clear, io.csr(0), cfg)

  StatusCSR(status, io.csr(1), cfg)

  io.irq <> InterruptController(addressGeneratorRead.io.ctl.busy, addressGeneratorWrite.io.ctl.busy,
    io.csr(2), io.csr(3), cfg)

  clear := Cat(readerStart, writerStart) & ~Cat(control(5), control(4))

  readerStart := ((!readerSyncOld && readerSync) || control(3)) && control(1)
  writerStart := ((!writerSyncOld && writerSync) || control(2)) && control(0)

  addressGeneratorRead.io.ctl.start := readerStart
  addressGeneratorRead.io.ctl.startAddress := SimpleCSR(io.csr(4), cfg)
  addressGeneratorRead.io.ctl.lineLength := SimpleCSR(io.csr(5), cfg)
  addressGeneratorRead.io.ctl.lineCount := SimpleCSR(io.csr(6), cfg)
  addressGeneratorRead.io.ctl.lineGap := SimpleCSR(io.csr(7), cfg)

  addressGeneratorWrite.io.ctl.start := writerStart
  addressGeneratorWrite.io.ctl.startAddress := SimpleCSR(io.csr(8), cfg)
  addressGeneratorWrite.io.ctl.lineLength := SimpleCSR(io.csr(9), cfg)
  addressGeneratorWrite.io.ctl.lineCount := SimpleCSR(io.csr(10), cfg)
  addressGeneratorWrite.io.ctl.lineGap := SimpleCSR(io.csr(11), cfg)

  StatusCSR(version, io.csr(12), cfg)
  StatusCSR(encConfig, io.csr(13), cfg)

  for (i <- 14 until cfg.controlRegCount) {
    SimpleCSR(io.csr(i), cfg)
  }

  transferSplitterRead.io.xferIn <> addressGeneratorRead.io.xfer
  io.xferRead <> transferSplitterRead.io.xferOut

  transferSplitterWrite.io.xferIn <> addressGeneratorWrite.io.xfer
  io.xferWrite <> transferSplitterWrite.io.xferOut

}
