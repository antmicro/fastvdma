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

class TransferSplitter(val addressWidth: Int, val dataWidth: Int,
    val maxLength: Int, val canCrossBarrier: Boolean, dmaConfig: DMAConfig
) extends DMAModule(dmaConfig) {
  val io = IO(new Bundle {
    val xferIn = Flipped(new XferDescBundle(addressWidth))
    val xferOut = new XferDescBundle(addressWidth)
  })

  if (maxLength != 0) {
    val sIdle :: sSplit :: sSplitWait :: Nil = Enum(3)

    val dataBytes: Int = dataWidth / 8

    val barrierBytes: Int = 4096

    val address_i = RegInit(0.U(addressWidth.W))
    val length_i = RegInit(0.U(addressWidth.W))

    val address_o = RegInit(0.U(addressWidth.W))
    val length_o = RegInit(0.U(addressWidth.W))

    val first_i = RegInit(false.B)
    val first_o = RegInit(false.B)

    val done = RegInit(false.B)
    val valid = RegInit(false.B)

    val state = RegInit(sIdle)

    io.xferIn.done := done
    io.xferOut.valid := valid

    io.xferOut.first := first_o
    io.xferOut.address := address_o
    io.xferOut.length := length_o

    switch(state) {
      is(sIdle) {
        done := false.B

        when(io.xferIn.valid) {
          address_i := io.xferIn.address
          length_i := io.xferIn.length
          first_i := io.xferIn.first
          state := sSplit
        }
      }
      is(sSplit) {
        address_o := address_i
        first_o := first_i
        valid := true.B
        state := sSplitWait

        when(length_i > maxLength.U) {
          if (canCrossBarrier) {
            length_o := maxLength.U
            length_i := length_i - maxLength.U
            address_i := address_i + maxLength.U * dataBytes.U
          } else {
            val bytesToBarrier = barrierBytes.U - address_i % barrierBytes.U
            when(bytesToBarrier < maxLength.U * dataBytes.U) {
              length_o := bytesToBarrier / dataBytes.U
              length_i := length_i - bytesToBarrier / dataBytes.U
              address_i := address_i + bytesToBarrier
            }.otherwise {
              length_o := maxLength.U
              length_i := length_i - maxLength.U
              address_i := address_i + maxLength.U * dataBytes.U
            }
          }

        }.otherwise {
          if (canCrossBarrier) {
            length_o := length_i
            length_i := 0.U
            address_i := address_i + length_i * dataBytes.U
          } else {
            val bytesToBarrier = barrierBytes.U - address_i % barrierBytes.U
            when(bytesToBarrier < length_i * dataBytes.U) {
              length_o := bytesToBarrier / dataBytes.U
              length_i := length_i - bytesToBarrier / dataBytes.U
              address_i := address_i + bytesToBarrier
            }.otherwise {
              length_o := length_i
              length_i := 0.U
              address_i := address_i + length_i * dataBytes.U
            }
          }
        }
      }
      is(sSplitWait) {
        valid := false.B
        first_i := false.B
        when(io.xferOut.done) {
          when(length_i > 0.U) {
            state := sSplit
          }.otherwise {
            state := sIdle
            done := true.B
          }
        }
      }
    }
  } else {
    io.xferOut <> io.xferIn
  }
}
