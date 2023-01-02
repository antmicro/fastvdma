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

import chiseltest.iotesters.PeekPokeTester

class InterruptControllerTest(dut : InterruptController) extends PeekPokeTester(dut){
  poke(dut.io.writeBusy, 0)
  poke(dut.io.readBusy, 0)

  poke(dut.io.imr.dataRead, 0)
  poke(dut.io.imr.dataWrite, 0)

  poke(dut.io.isr.dataRead, 0)
  poke(dut.io.isr.dataWrite, 0)

  step(5)

  poke(dut.io.imr.dataOut, 0x3)
  poke(dut.io.imr.dataWrite, 1)

  step(1)

  poke(dut.io.imr.dataWrite, 0)

  step(1)

  poke(dut.io.writeBusy, 1)
  poke(dut.io.readBusy, 1)

  step(5)

  poke(dut.io.writeBusy, 1)
  poke(dut.io.readBusy, 0)

  step(5)

  poke(dut.io.writeBusy, 0)
  poke(dut.io.readBusy, 0)

  step(5)

  poke(dut.io.isr.dataOut, 0x3)
  poke(dut.io.isr.dataWrite, 1)

  step(1)

  poke(dut.io.isr.dataWrite, 0)

  step(5)

}
