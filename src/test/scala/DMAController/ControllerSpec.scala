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

import chiseltest.ChiselScalatestTester
import org.scalatest.flatspec.AnyFlatSpec
import DMAController.DMAConfig._
import chiseltest._

class ControllerSpec extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "ControllerSpec"
  val dmaConfigMM2MM = new DMAConfig("AXI_AXIL_AXI")
  it should "perform 2D MM2MM transfer with stride mem to mem" in {
    test(new DMATop()(dmaConfigMM2MM))
      .withAnnotations(Seq(WriteVcdAnnotation))
      .runPeekPoke(dut =>
        new ImageTransfer(dut, new DMAFullMem(dut))(dmaConfigMM2MM)
      )
  }

  val dmaConfigS2MM = new DMAConfig("AXIS_AXIL_AXI")
  it should "perform 2D S2MM transfer with stride stream to mem" in {
    test(new DMATop()(dmaConfigS2MM))
      .withAnnotations(Seq(WriteVcdAnnotation))
      .runPeekPoke(dut =>
        new ImageTransfer(dut, new DMAFullStream(dut))(dmaConfigS2MM)
      )
  }

  val dmaConfigMM2MM64 = new DMAConfig("AXI_AXIL_AXI", addrWidth = 64)
  it should "perform 2D MM2MM 64-bit start address transfer with stride mem to mem" in {
    test(new DMATop()(dmaConfigMM2MM64))
      .withAnnotations(Seq(WriteVcdAnnotation))
      .runPeekPoke(dut => {
          val mem = new DMAFullMem(dut,
                                   readerBase = 0x4185941c00100000L,
                                   writerBase = 0x2222222222222224L)
          new ImageTransfer(dut, mem)(dmaConfigMM2MM64)
        }
      )
  }

  val dmaConfigS2MM64 = new DMAConfig("AXIS_AXIL_AXI", addrWidth = 64)
  it should "perform 2D S2MM 64-bit start address transfer with stride stream to mem" in {
    test(new DMATop()(dmaConfigS2MM64))
      .withAnnotations(Seq(WriteVcdAnnotation))
      .runPeekPoke(dut =>
        new ImageTransfer(dut, new DMAFullStream(dut, writerBase = 0x2112cafe00000000L))(dmaConfigS2MM64)
      )
  }
}
