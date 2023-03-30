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

package DMAController.DMAConfig

import chisel3._
import DMAController.Bus._
import DMAController.CSR.CSR
import DMAController.Frontend._
import DMAController.Worker.{InterruptBundle, WorkerCSRWrapper, SyncBundle}
import chisel3.util.Queue

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

object DMAIOConfig extends Bundle{
  import DMATop._
/* Supported buses */
  val AXI = 0
  val AXIL = 1
  val AXIS = 2
  val WB = 3
  val PWB = 4

  val cfg = System.getenv("DMACONFIG")

// DMA configuration options:
  var configuration = Map(
// AXI4 <-> AXI4Lite <-> AXI4
  "AXI_AXIL_AXI" -> (AXI, AXIL, AXI),
// AXI4 <-> Wishbone Slave <-> AXI4
  "AXI_WB_AXI" -> (AXI, WB, AXI),
// AXI4 <-> AXI4Lite <-> AXIStream
  "AXI_AXIL_AXIS" -> (AXI, AXIL, AXIS),
// AXI4 <-> Wishbone Slave <-> AXIStream
  "AXI_WB_AXIS" -> (AXI, WB, AXIS),
// AXI4 <-> AXI4Lite <-> Wishbone Master
  "AXI_AXIL_WB" -> (AXI, AXIL, WB),
// AXI4 <-> Wishbone Slave <-> Wishbone Master
  "AXI_WB_WB" -> (AXI, WB, WB),
// AXI4 <-> AXI4Lite <-> Pipelined Wishbone Master
  "AXI_AXIL_PWB" -> (AXI, AXIL, PWB),
// AXI4 <-> Wishbone Slave <-> Pipelined Wishbone Master
  "AXI_WB_PWB" -> (AXI, WB, PWB),
// AXIStream <-> AXI4Lite <-> AXI4
  "AXIS_AXIL_AXI" -> (AXIS, AXIL, AXI),
// AXIStream <-> Wishbone Slave <-> AXI4
  "AXIS_WB_AXI" -> (AXIS, WB, AXI),
// AXIStream <-> AXI4Lite <-> AXIStream
  "AXIS_AXIL_AXIS" -> (AXIS, AXIL, AXIS),
// AXIStream <-> Wishbone Slave <-> AXIStream
  "AXIS_WB_AXIS" -> (AXIS, WB, AXIS),
// AXIStream <-> AXI4Lite <-> Wishbone Master
  "AXIS_AXIL_WB" -> (AXIS, AXIL, WB),
// AXIStream <-> Wishbone Slave <-> Wishbone Master
  "AXIS_WB_WB" -> (AXIS, WB, WB),
// AXIStream <-> AXI4Lite <-> Pipelined Wishbone Master
  "AXIS_AXIL_WB" -> (AXIS, AXIL, WB),
// AXIStream <-> Wishbone Slave <-> Pipelined Wishbone Master
  "AXIS_WB_WB" -> (AXIS, WB, WB),
// Wishbone Master <-> AXI4Lite <-> AXI4
  "WB_AXIL_AXI" -> (WB, AXIL, AXI),
// Wishbone Master <-> Wishbone Slave <-> AXI4
  "WB_WB_AXI" -> (WB, WB, AXI),
// Wishbone Master <-> AXI4Lite <-> AXIStream
  "WB_AXIL_AXIS" -> (WB, AXIL, AXIS),
// Wishbone Master <-> Wishbone Slave <-> AXIStream
  "WB_WB_AXIS" -> (WB, WB, AXIS),
// Wishbone Master <-> AXI4Lite <-> Wishbone Master
  "WB_AXIL_WB" -> (WB, AXIL, WB),
// Wishbone Master <-> Wishbone Slave <-> Wishbone Master
  "WB_WB_WB" -> (WB, WB, WB),
// Wishbone Master <-> AXI4Lite <-> Pipelined Wishbone Master
  "WB_AXIL_PWB" -> (WB, AXIL, PWB),
// Wishbone Master <-> Wishbone Slave <-> Pipelined Wishbone Master
  "WB_WB_PWB" -> (WB, WB, PWB),
// Pipelined Wishbone Master <-> AXI4Lite <-> AXI4
  "PWB_AXIL_AXI" -> (PWB, AXIL, AXI),
// Pipelined Wishbone Master <-> Wishbone Slave <-> AXI4
  "PWB_WB_AXI" -> (PWB, WB, AXI),
// Pipelined Wishbone Master <-> AXI4Lite <-> AXIStream
  "PWB_AXIL_AXIS" -> (PWB, AXIL, AXIS),
// Pipelined Wishbone Master <-> Wishbone Slave <-> AXIStream
  "PWB_WB_AXIS" -> (PWB, WB, AXIS),
// Pipelined Wishbone Master <-> AXI4Lite <-> Wishbone Master
  "PWB_AXIL_WB" -> (PWB, AXIL, WB),
// Pipelined  Wishbone Master <-> Wishbone Slave <-> Wishbone Master
  "PWB_WB_WB" -> (PWB, WB, WB),
// Pipelined Wishbone Master <-> AXI4Lite <-> Pipelined Wishbone Master
  "PWB_AXIL_PWB" -> (PWB, AXIL, PWB),
// Pipelined Wishbone Master <-> Wishbone Slave <-> Pipelined Wishbone Master
  "PWB_WB_PWB" -> (PWB, WB, PWB))

  def getConfig() : (Int, Int, Int) = {
    try {
      configuration.apply(cfg)
    }
    catch {
      case ex :Exception => throw new Exception("Unsupported DMA configuration: " + cfg);
    }
  }
}

object Register {
  val Ctrl = 0x00
  val Status =0x04
  val InterruptMask = 0x08
  val InterruptStatus = 0x0c
  val ReaderStartAddr = 0x10
  val ReaderLineLen = 0x14
  val ReaderLineCnt = 0x18
  val ReaderStride = 0x1c
  val WriterStartAddr = 0x20
  val WriterLineLen = 0x24
  val WriterLineCnt = 0x28
  val WriterStride = 0x2c
  val Version = 0x30
  val Configuration = 0x34
}
