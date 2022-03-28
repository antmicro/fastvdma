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

package DMAController

import DMAController.Frontend._
import DMAController.Worker.{AddressGenerator, AddressGeneratorTest, InterruptController, InterruptControllerTest, TransferSplitter, TransferSplitterTest}
import org.scalatest.{FlatSpec, Matchers}

class ComponentSpec extends FlatSpec with Matchers{
  behavior of "ComponentSpec"

  it should "generate addresses" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new AddressGenerator(32, 32)) { dut =>
      new AddressGeneratorTest(dut)
    } should be(true)
  }
  it should "split transfers" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new TransferSplitter(32, 32, 256, false)) { dut =>
      new TransferSplitterTest(dut)
    } should be(true)
  }
  it should "perform AXI Stream master transfers" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new AXIStreamMaster(32, 32)) { dut =>
      new AXIStreamMasterTest(dut)
    } should be(true)
  }
  it should "perform AXI Stream slave transfers" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new AXIStreamSlave(32, 32)) { dut =>
      new AXIStreamSlaveTest(dut)
    } should be(true)
  }
  it should "perform AXI4 write transfers" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new AXI4Writer(32, 32)) { dut =>
      new AXI4WriterTest(dut)
    } should be(true)
  }
  it should "perform AXI4 read transfers" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new AXI4Reader(32, 32)) { dut =>
      new AXI4ReaderTest(dut)
    } should be(true)
  }
  it should "perform Wishbone write transfers" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new WishboneClassicPipelinedWriter(32, 32)) { dut =>
      new WishboneWriterTest(dut)
    } should be(true)
  }
  it should "trigger interrupts" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new InterruptController) { dut =>
      new InterruptControllerTest(dut)
    } should be(true)
  }
}
