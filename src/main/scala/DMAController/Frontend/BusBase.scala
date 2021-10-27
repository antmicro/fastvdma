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

package DMAController.Frontend
import DMAController.Bus._
import DMAController.CSR.{CSR, CSRBusBundle}
import DMAController.Worker.{WorkerCSRWrapper, XferDescBundle}
import DMAController.DMAConfig.DMATop
import DMAController.DMAConfig.DMAIOConfig._
import chisel3._
import chisel3.util._

abstract class IOBus[+T] extends Module {
  val io : Bundle {
    val bus : T
    val dataIO : DecoupledIO[UInt]
    val xfer : XferDescBundle
  }
}

abstract class CSRBus [+T] extends Module {
  val io : Bundle {
    val bus : CSRBusBundle
    val ctl : T
  }
}

class Bus {
  def getCSR(busType: Int) = busType match {
    case AXIL => new AXI4LiteCSR(DMATop.addrWidth)
    case WB => new WishboneCSR(DMATop.addrWidth)
  }

  def getReader(busType: Int) = busType match {
    case AXI => new AXI4Reader(DMATop.addrWidth, DMATop.readDataWidth)
    case AXIS => new AXIStreamSlave(DMATop.addrWidth, DMATop.readDataWidth)
    case WB => new WishboneClassicReader(DMATop.addrWidth, DMATop.readDataWidth)
    case PWB => new WishboneClassicPipelinedReader(DMATop.addrWidth, DMATop.readDataWidth)
  }

  def getWriter(busType: Int) = busType match {
    case AXI => new AXI4Writer(DMATop.addrWidth, DMATop.readDataWidth)
    case AXIS => new AXIStreamMaster(DMATop.addrWidth, DMATop.readDataWidth)
    case WB => new WishboneClassicWriter(DMATop.addrWidth, DMATop.readDataWidth)
    case PWB => new WishboneClassicPipelinedWriter(DMATop.addrWidth, DMATop.readDataWidth)
  }

  def getControlBus(busType: Int) = busType match {
    case AXIL => Flipped(new AXI4Lite(DMATop.controlAddrWidth, DMATop.controlDataWidth))
    case WB => new WishboneSlave(DMATop.addrWidth, DMATop.controlDataWidth)
  }

  def getReaderBus(busType: Int) =  busType match {
    case AXI => new AXI4(DMATop.addrWidth, DMATop.readDataWidth)
    case AXIS => Flipped(new AXIStream(DMATop.readDataWidth))
    case WB | PWB => new WishboneMaster(DMATop.addrWidth, DMATop.readDataWidth)
  }

  def getWriterBus(busType: Int) =  busType match {
    case AXI => new AXI4(DMATop.addrWidth, DMATop.readDataWidth)
    case AXIS => new AXIStream(DMATop.readDataWidth)
    case WB | PWB => new WishboneMaster(DMATop.addrWidth, DMATop.readDataWidth)
  }
}
