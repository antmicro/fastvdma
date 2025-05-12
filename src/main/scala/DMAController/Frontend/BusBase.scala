/*
Copyright (C) 2019-2024 Antmicro

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

abstract class IOBus[T <: BusIf](config: DMAConfig) extends DMAModule(config) {
  val io : Bundle {
    val bus : T
    val dataIO : DecoupledIO[UInt]
    val xfer : XferDescBundle
  }
}

abstract class CSRBus[T <: BusIf] (config: DMAConfig) extends DMAModule(config) {
  val io : Bundle {
    val bus : CSRBusBundle
    val ctl : T
  }
}

class Bus(config: DMAConfig) {
  import DMAConfig.{AXI, AXIL, AXIS, WB, PWB}

  def getCSR(busType: Int) = busType match {
    case AXIL => new AXI4LiteCSR(config.addrWidth, config.controlDataWidth, config.controlRegCount, config)
    case WB => new WishboneCSR(config.addrWidth, config.controlDataWidth, config.controlRegCount, config)
  }

  def getReader(busType: Int) = busType match {
    case AXI => new AXI4Reader(config.addrWidth, config.readDataWidth, config)
    case AXIS => new AXIStreamSlave(config.addrWidth, config.readDataWidth, config)
    case WB => new WishboneClassicReader(config.addrWidth, config.readDataWidth, config)
    case PWB => new WishboneClassicPipelinedReader(config.addrWidth, config.readDataWidth, config)
  }

  def getWriter(busType: Int) = busType match {
    case AXI => new AXI4Writer(config.addrWidth, config.readDataWidth, config)
    case AXIS => new AXIStreamMaster(config.addrWidth, config.readDataWidth, config)
    case WB => new WishboneClassicWriter(config.addrWidth, config.readDataWidth, config)
    case PWB => new WishboneClassicPipelinedWriter(config.addrWidth, config.readDataWidth, config)
  }

  def getControlBus(busType: Int) = busType match {
    case AXIL => Flipped(new AXI4Lite(config.controlAddrWidth, config.controlDataWidth))
    case WB => new WishboneSlave(config.addrWidth, config.controlDataWidth)
  }

  def getReaderBus(busType: Int) =  busType match {
    case AXI => new AXI4(config.addrWidth, config.readDataWidth)
    case AXIS => Flipped(new AXIStream(config.readDataWidth))
    case WB | PWB => new WishboneMaster(config.addrWidth, config.readDataWidth)
  }

  def getWriterBus(busType: Int) =  busType match {
    case AXI => new AXI4(config.addrWidth, config.readDataWidth)
    case AXIS => new AXIStream(config.readDataWidth)
    case WB | PWB => new WishboneMaster(config.addrWidth, config.readDataWidth)
  }
}
