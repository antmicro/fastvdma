/*
Copyright (C) 2019-2024 Antmicro

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
import DMAController.Worker.{XferDescBundle, WorkerCSRWrapper}
import DMAController.CSR.CSR
import chisel3._
import chisel3.util._
import DMAController.DMAConfig._

class AXIStreamSlave(val addrWidth: Int, val dataWidth: Int, dmaConfig: DMAConfig)
    extends IOBus[AXIStream](dmaConfig) {
  val io = IO(new Bundle{
    val bus = Flipped(new AXIStream(dataWidth))

    val dataIO = EnqIO(UInt(dataWidth.W))

    val xfer = Flipped(new XferDescBundle(addrWidth))
  })

  val sIdle :: sTransfer :: sDone :: Nil = Enum(3)

  val state = RegInit(sIdle)

  val done = RegInit(false.B)

  val enable = RegInit(false.B)

  val length = RegInit(0.U(addrWidth.W))

  val ready = WireInit(io.dataIO.ready && enable)
  val valid = WireInit(io.bus.tvalid && enable)

  io.dataIO.valid := valid
  io.bus.tready := ready

  io.dataIO.bits := io.bus.tdata

  io.xfer.done := done

  switch(state) {
    is(sIdle) {
      done := false.B
      enable := false.B

      when(io.xfer.valid) {
        state := sTransfer
        length := io.xfer.length
        enable := true.B
      }
    }
    is(sTransfer) {
      when(ready && valid) {
        length := length - 1.U
        when(length === 1.U) {
          state := sDone
          enable := false.B
        }
      }
    }
    is(sDone) {
      state := sIdle
      done := true.B
    }
  }

}
