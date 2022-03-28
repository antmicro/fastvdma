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

package DMAController.Worker

import chisel3.iotesters._

class TransferSplitterTest(dut : TransferSplitter) extends PeekPokeTester(dut){
  poke(dut.io.xferIn.address, 16)
  poke(dut.io.xferIn.length, 2048)
  poke(dut.io.xferIn.valid, 0)
  poke(dut.io.xferOut.done, 0)

  step(10)

  poke(dut.io.xferIn.valid, 1)
  step(1)
  poke(dut.io.xferIn.valid, 0)

  while(peek(dut.io.xferIn.done) == 0){
    step(10)
    poke(dut.io.xferOut.done, 1)
    step(1)
    poke(dut.io.xferOut.done, 0)
  }

}
