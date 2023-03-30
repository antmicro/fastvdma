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

package DMAController.Worker

import chisel3.iotesters._

class AddressGeneratorTest(dut : AddressGenerator) extends PeekPokeTester(dut){
  poke(dut.io.ctl.start, 0)
  poke(dut.io.ctl.startAddress, 0x80000000)
  poke(dut.io.ctl.lineLength, 21)
  poke(dut.io.ctl.lineCount, 1004)
  poke(dut.io.ctl.lineGap, 37)
  poke(dut.io.xfer.done, 0)

  step(10)

  poke(dut.io.ctl.start, 1)

  step(10)

  for(i <- 1 to 1004){
    poke(dut.io.xfer.done, 1)
    step(1)
    poke(dut.io.xfer.done, 0)
    step(10)
  }

}
