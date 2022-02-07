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

package DMAController.Frontend

import chisel3.iotesters._
import DMAController.TestUtil.WaitRange._

class AXIStreamSlaveTest(dut : AXIStreamSlave) extends PeekPokeTester(dut){
  val data = 0xdeadbeef
  val maxStep = 500

  poke(dut.io.bus.tvalid, 1)
  poke(dut.io.bus.tdata, data)
  poke(dut.io.dataIO.ready, 1)
  poke(dut.io.xfer.length, 100)
  poke(dut.io.xfer.valid, 0)

  step(10)

  poke(dut.io.xfer.valid, 1)

  step(1)

  poke(dut.io.xfer.valid, 0)

  assert(waitRange(0, maxStep, {() =>
    step(1)
  peek(dut.io.xfer.done) == 1}))

  val data_o = peek(dut.io.dataIO.bits)
  // Compare hex to avoid sign issues
  assert(f"$data%x" == f"$data_o%x")
}
