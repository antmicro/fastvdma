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

import DMAController.Bus.WishboneMaster
import DMAController.Worker.{XferDescBundle, WorkerCSRWrapper}
import DMAController.CSR.CSR
import chisel3._
import chisel3.util._
import DMAController.DMAConfig._

class WishboneClassicReader(val addrWidth: Int, val dataWidth: Int,
    dmaConfig: DMAConfig) extends IOBus[WishboneMaster](dmaConfig) {
  val io = IO(new Bundle{
    val bus = new WishboneMaster(addrWidth, dataWidth)
    val dataIO = EnqIO(UInt(dataWidth.W))
    val xfer = Flipped(new XferDescBundle(addrWidth))
  })

  val sIdle :: sWait :: Nil = Enum(2)

  val state = RegInit(sIdle)

  val stbCnt = RegInit(0.U(addrWidth.W))
  val adr = RegInit(0.U(addrWidth.W))
  val stb = WireInit(stbCnt =/= 0.U && io.dataIO.ready)
  val cyc = WireInit(stb)
  val ack = WireInit(io.bus.ack_i)

  val ready = WireInit(cyc && stb && ack)

  val done = RegInit(false.B)

  io.dataIO.bits := io.bus.dat_i
  io.dataIO.valid := ready

  io.bus.dat_o := 0.U
  io.bus.we_o := false.B
  io.bus.sel_o := ~0.U((dataWidth / 8).W)
  io.bus.adr_o := adr(addrWidth - 1, log2Ceil(dataWidth / 8))
  io.bus.cyc_o := cyc
  io.bus.stb_o := stb

  io.xfer.done := done

  switch(state){
    is(sIdle){
      done := false.B
      when(io.xfer.valid) {
        state := sWait
        stbCnt := io.xfer.length
        adr := io.xfer.address
      }
    }
    is(sWait){
      when(stbCnt === 0.U){
        state := sIdle
        done := true.B
      }
    }
  }

  when(stbCnt =/= 0.U && ready){
    adr := adr + (dataWidth / 8).U
    stbCnt := stbCnt - 1.U
  }

}
