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

class AXI4ReaderTest(dut: AXI4Reader) extends PeekPokeTester(dut){
  val transferLen = 4
  var currentLen = 0

  poke(dut.io.xfer.valid, 0)
  poke(dut.io.xfer.address, 0x40000000)
  poke(dut.io.xfer.length, transferLen)

  poke(dut.io.dataIO.ready, 0)

  poke(dut.io.bus.ar.arready, 0)

  poke(dut.io.bus.r.rvalid, 0)
  poke(dut.io.bus.r.rdata, 0x45671234)
  poke(dut.io.bus.r.rlast, 0)

  step(5)

  poke(dut.io.xfer.valid, 1)

  step(1)

  poke(dut.io.bus.ar.arready, 1)

  while(peek(dut.io.bus.ar.arvalid) != 1){
    printf("arvalid\n")
    step(1)
  }

  step(1)

  poke(dut.io.bus.ar.arready, 0)

  step(1)

  poke(dut.io.dataIO.ready, 1)
  poke(dut.io.bus.r.rvalid, 1)

  while(transferLen > (currentLen + 1)){
    printf("rready\n")
    if(peek(dut.io.bus.r.rready) == 1){
      currentLen += 1
    }
    step(1)
  }

  poke(dut.io.bus.r.rlast, 1)

  while(peek(dut.io.bus.r.rready) != 1){
    printf("rlast\n")
    step(1)
  }

  step(1)

  poke(dut.io.bus.r.rvalid, 0)
  poke(dut.io.bus.r.rlast, 0)

  while(peek(dut.io.xfer.done) != 1){
    printf("done")
    step(1)
  }

  step(5)

}
