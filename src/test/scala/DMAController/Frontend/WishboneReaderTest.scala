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

package DMAController.Frontend

import chisel3.iotesters._
import DMAController.TestUtil.WaitRange._

class WishboneReaderTest(dut: WishboneClassicPipelinedReader) extends PeekPokeTester(dut) {
  val maxStep = 100
  val data = 0x7654321

  poke(dut.io.xfer.length, 50)
  poke(dut.io.xfer.valid, 0)

  poke(dut.io.bus.dat_i, data)
  poke(dut.io.dataIO.ready, 1)

  poke(dut.io.bus.ack_i, 1)

  assert(peek(dut.io.bus.we_o) == 0)

  step(1)

  poke(dut.io.xfer.valid, 1)

  step(1)

  poke(dut.io.xfer.valid, 0)

  assert(waitRange(0, maxStep, {() =>
    step(1)
    peek(dut.io.xfer.done) == 1
  }))
  assert(peek(dut.io.dataIO.bits) == data)
}
