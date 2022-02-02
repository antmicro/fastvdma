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

import DMAController.CSR._
import DMAController.DMAConfig._
import DMAUtils._
import chisel3._
import chisel3.util.Cat

class WorkerCSRWrapper(addrWidth : Int, readerDataWidth : Int, writerDataWidth : Int, readerMaxBurst : Int,
                       writerMaxBurst : Int, reader4KBarrier : Boolean, writer4KBarrier : Boolean) extends DMAModule{
  val io = IO(new Bundle{
    val csr: Vec[CSRRegBundle] = Vec(DMATop.controlRegCount, Flipped(new CSRRegBundle()))
    val irq = new InterruptBundle
    val sync = new SyncBundle
    val xferRead = new XferDescBundle(addrWidth)
    val xferWrite = new XferDescBundle(addrWidth)
  })

  val addressGeneratorRead = Module(new AddressGenerator(addrWidth, readerDataWidth))
  val transferSplitterRead = Module(new TransferSplitter(addrWidth, readerDataWidth, readerMaxBurst, reader4KBarrier))

  val addressGeneratorWrite = Module(new AddressGenerator(addrWidth, writerDataWidth))
  val transferSplitterWrite = Module(new TransferSplitter(addrWidth, writerDataWidth, writerMaxBurst, writer4KBarrier))

  val status = RegNext(Cat(addressGeneratorRead.io.ctl.busy, addressGeneratorWrite.io.ctl.busy))

  val readerSync = RegNext(io.sync.readerSync)
  val readerSyncOld = RegNext(readerSync)

  val writerSync = RegNext(io.sync.writerSync)
  val writerSyncOld = RegNext(writerSync)

  val readerStart = RegInit(false.B)
  val writerStart = RegInit(false.B)

  val control = Wire(UInt())
  val clear = Wire(UInt())

  val envTag = System.getenv("TAG")
  val tag = if (envTag.isEmpty()) "v0.0" else envTag
  val version = RegInit(tag.filter(_.isDigit).toInt.U)
  val (in, csr, out) = DMAIOConfig.getConfig()
  val config = RegInit((in << 8 | csr << 4 | out).U(addrWidth.W))

  control := ClearCSR(clear, io.csr(0))

  StatusCSR(status, io.csr(1))

  io.irq <> InterruptController(addressGeneratorRead.io.ctl.busy, addressGeneratorWrite.io.ctl.busy,
    io.csr(2), io.csr(3))

  clear := Cat(readerStart, writerStart) & ~Cat(control(5), control(4))

  readerStart := ((!readerSyncOld && readerSync) || control(3)) && control(1)
  writerStart := ((!writerSyncOld && writerSync) || control(2)) && control(0)

  addressGeneratorRead.io.ctl.start := readerStart
  addressGeneratorRead.io.ctl.startAddress := SimpleCSR(io.csr(4))
  addressGeneratorRead.io.ctl.lineLength := SimpleCSR(io.csr(5))
  addressGeneratorRead.io.ctl.lineCount := SimpleCSR(io.csr(6))
  addressGeneratorRead.io.ctl.lineGap := SimpleCSR(io.csr(7))

  addressGeneratorWrite.io.ctl.start := writerStart
  addressGeneratorWrite.io.ctl.startAddress := SimpleCSR(io.csr(8))
  addressGeneratorWrite.io.ctl.lineLength := SimpleCSR(io.csr(9))
  addressGeneratorWrite.io.ctl.lineCount := SimpleCSR(io.csr(10))
  addressGeneratorWrite.io.ctl.lineGap := SimpleCSR(io.csr(11))

  StatusCSR(version, io.csr(12))
  StatusCSR(version, io.csr(13))

  for(i <- 14 until DMATop.controlRegCount){
    SimpleCSR(io.csr(i))
  }

  transferSplitterRead.io.xferIn <> addressGeneratorRead.io.xfer
  io.xferRead <> transferSplitterRead.io.xferOut

  transferSplitterWrite.io.xferIn <> addressGeneratorWrite.io.xfer
  io.xferWrite <> transferSplitterWrite.io.xferOut

}
