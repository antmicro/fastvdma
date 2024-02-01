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

import DMAController.DMAConfig._
import DMAUtils.DMAModule
import chisel3._
import chisel3.util._

class AddressGenerator(val addrWidth: Int, val dataWidth: Int,
    dmaConfig: DMAConfig) extends DMAModule(dmaConfig) {
  val io = IO(new Bundle{
    val ctl = new AddressGeneratorCtlBundle(addrWidth)
    val xfer = new XferDescBundle(addrWidth)
  })

  val sIdle :: sLine :: sLineWait :: sDone :: Nil = Enum(4)

  val state = RegInit(sIdle)

  val lineCount = RegInit(0.U(addrWidth.W))
  val lineGap = RegInit(0.U(addrWidth.W))

  val address_o = RegInit(0.U(addrWidth.W))
  val address_i = RegInit(0.U(addrWidth.W))
  val length_o = RegInit(0.U(addrWidth.W))
  val length_i = RegInit(0.U(addrWidth.W))
  val valid = RegInit(false.B)
  val first = RegInit(false.B)
  val busy = RegInit(false.B)

  io.xfer.address := address_o
  io.xfer.length := length_o
  io.xfer.valid := valid
  io.xfer.first := first
  io.ctl.busy := busy

  when(state === sIdle) {
    busy := false.B
  }.otherwise {
    busy := true.B
  }

  switch(state) {
    is(sIdle) {
      when(io.ctl.start) {
        state := sLine

        address_i := io.ctl.startAddress
        length_i := io.ctl.lineLength
        lineCount := io.ctl.lineCount
        lineGap := io.ctl.lineGap
        first := true.B
      }
    }
    is(sLine) {

      valid := true.B
      address_o := address_i
      length_o := length_i
      address_i := address_i + (length_i * (dataWidth / 8).U) + (lineGap * (dataWidth / 8).U)

      lineCount := lineCount - 1.U
      state := sLineWait
    }
    is(sLineWait) {
      valid := false.B
      first := false.B
      when(io.xfer.done) {
        when(lineCount > 0.U) {
          state := sLine
        }.otherwise {
          state := sIdle
        }
      }
    }
  }

}
