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

import chiseltest.ChiselScalatestTester
import org.scalatest.flatspec.AnyFlatSpec

class ControllerSpec extends AnyFlatSpec with ChiselScalatestTester{
  behavior of "ControllerSpec"
  val dma_config = System.getenv("DMACONFIG")
  dma_config match {
    case "AXI_AXIL_AXI" =>
      it should "perform 2D MM2MM transfer with stride mem to mem" in {
        test(new DMATop).runPeekPoke(dut => new ImageTransfer(dut, new DMAFullMem(dut)))
      }

    case "AXIS_AXIL_AXI" =>
      it should "perform 2D S2MM transfer with stride stream to mem" in {
        test(new DMATop).runPeekPoke(dut => new ImageTransfer(dut, new DMAFullStream(dut)))
      }

    case _ => ()
  }
}
