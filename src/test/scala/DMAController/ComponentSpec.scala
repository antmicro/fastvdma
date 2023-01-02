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

import DMAController.Frontend._
import DMAController.Worker.{AddressGenerator, AddressGeneratorTest, InterruptController, InterruptControllerTest, TransferSplitter, TransferSplitterTest}
import org.scalatest.{FlatSpec, Matchers}
import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.flatspec.AnyFlatSpec

class ComponentSpec extends AnyFlatSpec with ChiselScalatestTester{
  behavior of "ComponentSpec"

  it should "generate addresses" in {
    test(new AddressGenerator(32, 32)).runPeekPoke(new AddressGeneratorTest(_))
  }
  it should "split transfers" in {
      test(new TransferSplitter(32, 32, 256, false)).runPeekPoke(new TransferSplitterTest(_))
  }
  it should "perform AXI Stream master transfers" in {
      test(new AXIStreamMaster(32, 32)).runPeekPoke(new AXIStreamMasterTest(_))
  }
  it should "perform AXI Stream slave transfers" in {
      test(new AXIStreamSlave(32, 32)).runPeekPoke(new AXIStreamSlaveTest(_))
  }
  it should "perform AXI4 write transfers" in {
      test(new AXI4Writer(32, 32)).runPeekPoke(new AXI4WriterTest(_))
  }
  it should "perform AXI4 read transfers" in {
      test(new AXI4Reader(32, 32)).runPeekPoke(new AXI4ReaderTest(_))
  }
  it should "perform Wishbone write transfers" in {
      test(new WishboneClassicPipelinedWriter(32, 32)).runPeekPoke(new WishboneWriterTest(_))
  }
  it should "perform Wishbone read transfers" in {
      test(new WishboneClassicPipelinedReader(32, 32)).runPeekPoke(new WishboneReaderTest(_))
  }
  it should "trigger interrupts" in {
      test(new InterruptController).runPeekPoke(new InterruptControllerTest(_))
  }
}
