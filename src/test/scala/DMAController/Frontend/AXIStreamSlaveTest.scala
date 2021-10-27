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

import chisel3.iotesters._

class AXIStreamSlaveTest(dut : AXIStreamSlave) extends PeekPokeTester(dut){
  poke(dut.io.bus.tvalid, 1)
  poke(dut.io.bus.tdata, 0xdeadbeef)
  poke(dut.io.dataIO.ready, 1)
  poke(dut.io.xfer.length, 300)
  poke(dut.io.xfer.valid, 0)

  step(10)

  poke(dut.io.xfer.valid, 1)

  step(1)

  poke(dut.io.xfer.valid, 0)

  step(500)

}
