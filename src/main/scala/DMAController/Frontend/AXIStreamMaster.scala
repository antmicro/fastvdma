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

import DMAController.Bus.AXIStream
import DMAController.Worker.XferDescBundle
import chisel3._
import chisel3.util._

class AXIStreamMaster(val addrWidth: Int, val dataWidth: Int) extends Module{
  val io = IO(new Bundle{
    val bus = new AXIStream(dataWidth)

    val dataIn = DeqIO(UInt(dataWidth.W))

    val xfer = Flipped(new XferDescBundle(addrWidth))
  })

  val sIdle :: sTransfer :: sDone :: Nil = Enum(3)

  val state = RegInit(sIdle)

  val done = RegInit(false.B)

  val enable = RegInit(false.B)
  val last = WireInit(false.B)
  val user = RegInit(false.B)

  val length = RegInit(0.U(addrWidth.W))

  val ready = WireInit(io.bus.tready && enable)
  val valid = WireInit(io.dataIn.valid && enable)

  io.bus.tvalid := valid
  io.dataIn.ready := ready

  io.bus.tdata := io.dataIn.bits
  io.bus.tlast := last
  io.bus.tuser := user

  io.xfer.done := done

  last := length === 1.U

  switch(state){
    is(sIdle){
      done := false.B
      enable := false.B

      when(io.xfer.valid){
        user := io.xfer.first
        state := sTransfer
        length := io.xfer.length
        enable := true.B
      }
    }
    is(sTransfer){
      when(ready && valid){
        user := false.B
        length := length - 1.U
        when(length === 1.U){
          state := sDone
          enable := false.B
        }
      }
    }
    is(sDone){
      state := sIdle
      done := true.B
    }
  }

}
