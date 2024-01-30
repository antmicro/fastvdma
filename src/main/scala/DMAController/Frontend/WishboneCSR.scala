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

import DMAController.Bus.WishboneSlave
import DMAController.CSR.CSRBusBundle
import DMAController.DMAConfig._
import chisel3._
import chisel3.util._

class WishboneCSR(addrWidth: Int, dataWidth: Int, regCount: Int,
    dmaConfig: DMAConfig) extends CSRBus[WishboneSlave](dmaConfig) {
  val io = IO(new Bundle {
    val ctl = new WishboneSlave(addrWidth, dataWidth)
    val bus = new CSRBusBundle(regCount, dataWidth)
  })

  val sIdle :: sAck :: Nil = Enum(2)

  val state = RegInit(sIdle)

  val ack = RegInit(false.B)

  val valid = WireInit(io.ctl.stb_i & io.ctl.cyc_i)

  switch(state) {
    is(sIdle) {
      ack := false.B
      when(io.ctl.stb_i & io.ctl.cyc_i) {
        state := sAck
        ack := true.B
      }
    }
    is(sAck) {
      ack := false.B
      state := sIdle
    }
  }

  io.ctl.stall_o := false.B
  io.ctl.err_o := false.B

  io.ctl.ack_o := ack
  io.bus.write := ack & io.ctl.we_i
  io.bus.read := ack & !io.ctl.we_i

  io.bus.dataOut := io.ctl.dat_i
  io.ctl.dat_o := io.bus.dataIn
  io.bus.addr := io.ctl.adr_i
}
