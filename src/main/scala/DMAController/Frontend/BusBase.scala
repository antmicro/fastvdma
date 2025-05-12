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

package DMAController.Frontend
import DMAController.Bus._
import DMAController.CSR.{CSR, CSRBusBundle}
import DMAController.Worker.{WorkerCSRWrapper, XferDescBundle}
import DMAUtils.DMAModule
import chisel3._
import chisel3.util._
import DMAController.DMADriver
import DMAController.DMAConfig._

abstract class IOBus[+T <: BusIf](implicit dmaConfig: DMAConfig) extends DMAModule {
  val io: Bundle {
    val bus: T
    val dataIO: DecoupledIO[UInt]
    val xfer: XferDescBundle
  }
}

abstract class CSRBus[+T <: BusIf](implicit dmaConfig: DMAConfig) extends DMAModule {
  val io: Bundle {
    val bus: CSRBusBundle
    val ctl: T
  }
}

object Bus {
  import DMAConfig.{AXI, AXIL, AXIS, WB, PWB}

  def getCSR(implicit dmaConfig: DMAConfig): CSRBus[BusIf] = {
    val (_, busType, _) = dmaConfig.getBusConfig()
    busType match {
      case AXIL => new AXI4LiteCSR(dmaConfig.addrWidth, dmaConfig.controlDataWidth, dmaConfig.controlRegCount)
      case WB => new WishboneCSR(dmaConfig.addrWidth, dmaConfig.controlDataWidth, dmaConfig.controlRegCount)
    }
  }

  def getReader(implicit dmaConfig: DMAConfig): IOBus[BusIf] = {
    val (busType, _, _) = dmaConfig.getBusConfig()
    busType match {
      case AXI  => new AXI4Reader(dmaConfig.addrWidth, dmaConfig.readDataWidth)
      case AXIS => new AXIStreamSlave(dmaConfig.addrWidth, dmaConfig.readDataWidth)
      case WB   => new WishboneClassicReader(dmaConfig.addrWidth, dmaConfig.readDataWidth)
      case PWB  => new WishboneClassicPipelinedReader(dmaConfig.addrWidth, dmaConfig.readDataWidth)
    }
  }

  def getWriter(implicit dmaConfig: DMAConfig): IOBus[BusIf] = {
    val (_, _, busType) = dmaConfig.getBusConfig()
    busType match {
      case AXI  => new AXI4Writer(dmaConfig.addrWidth, dmaConfig.readDataWidth)
      case AXIS => new AXIStreamMaster(dmaConfig.addrWidth, dmaConfig.readDataWidth)
      case WB   => new WishboneClassicWriter(dmaConfig.addrWidth, dmaConfig.readDataWidth)
      case PWB  => new WishboneClassicPipelinedWriter(dmaConfig.addrWidth, dmaConfig.readDataWidth)
    }
  }

  def getControlBus(implicit dmaConfig: DMAConfig): BusIf = {
    val (_, busType, _) = dmaConfig.getBusConfig()
    busType match {
      case AXIL => Flipped(new AXI4Lite(dmaConfig.controlAddrWidth, dmaConfig.controlDataWidth))
      case WB   => new WishboneSlave(dmaConfig.addrWidth, dmaConfig.controlDataWidth)
    }
  }

  def getReaderBus(implicit dmaConfig: DMAConfig): BusIf = {
    val (busType, _, _) = dmaConfig.getBusConfig()
    busType match {
      case AXI      => new AXI4(dmaConfig.addrWidth, dmaConfig.readDataWidth)
      case AXIS     => Flipped(new AXIStream(dmaConfig.readDataWidth))
      case WB | PWB => new WishboneMaster(dmaConfig.addrWidth, dmaConfig.readDataWidth)
    }
  }

  def getWriterBus(implicit dmaConfig: DMAConfig): BusIf = {
    val (_, _, busType) = dmaConfig.getBusConfig()
    busType match {
      case AXI      => new AXI4(dmaConfig.addrWidth, dmaConfig.readDataWidth)
      case AXIS     => new AXIStream(dmaConfig.readDataWidth)
      case WB | PWB => new WishboneMaster(dmaConfig.addrWidth, dmaConfig.readDataWidth)
    }
  }
}
