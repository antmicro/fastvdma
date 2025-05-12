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

import DMAController.Frontend._
import DMAController.Worker._
import chisel3._
import chiseltest._
import chiseltest.iotesters._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.flatspec.AnyFlatSpec
import DMAController.DMAConfig._

class ComponentSpec extends AnyFlatSpec with ChiselScalatestTester {
  val cfg = new DMAConfig("AXI_AXIL_AXI")
  val testAnnotations = Seq(WriteVcdAnnotation)

  def testFastVDMAComponent[T <: Module](
      dutGen: => T,
      tester: T => PeekPokeTester[T]
  ): Unit = {
    test(dutGen)
      .withAnnotations(testAnnotations)
      .runPeekPoke(tester)
  }

  behavior of "ComponentSpec"

  it should "generate addresses" in {
    testFastVDMAComponent(
      new AddressGenerator(32, 32, cfg),
      new AddressGeneratorTest(_)
    )
  }
  it should "split transfers" in {
    testFastVDMAComponent(
      new TransferSplitter(32, 32, 256, false, cfg),
      new TransferSplitterTest(_)
    )
  }
  it should "perform AXI Stream master transfers" in {
    testFastVDMAComponent(
      new AXIStreamMaster(32, 32, cfg),
      new AXIStreamMasterTest(_)
    )
  }
  it should "perform AXI Stream slave transfers" in {
    testFastVDMAComponent(
      new AXIStreamSlave(32, 32, cfg),
      new AXIStreamSlaveTest(_)
    )
  }
  it should "perform AXI4 write transfers" in {
    testFastVDMAComponent(
      new AXI4Writer(32, 32, cfg),
      new AXI4WriterTest(_))
  }
  it should "perform AXI4 read transfers" in {
    testFastVDMAComponent(
      new AXI4Reader(32, 32, cfg),
      new AXI4ReaderTest(_))
  }
  it should "perform Wishbone write transfers" in {
    testFastVDMAComponent(
      new WishboneClassicPipelinedWriter(32, 32, cfg),
      new WishboneWriterTest(_)
    )
  }
  it should "perform Wishbone read transfers" in {
    testFastVDMAComponent(
      new WishboneClassicPipelinedReader(32, 32, cfg),
      new WishboneReaderTest(_)
    )
  }
  it should "trigger interrupts" in {
    testFastVDMAComponent(
      new InterruptController(cfg),
      new InterruptControllerTest(_)
    )
  }
}
