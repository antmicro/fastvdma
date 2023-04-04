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

import DMAController.Bus._
import DMAController.Worker.{XferDescBundle, WorkerCSRWrapper}
import DMAController.CSR.CSR
import chisel3._
import chisel3.util._
import DMAController.DMAConfig._

class AXI4Writer(val addrWidth: Int, val dataWidth: Int, dmaConfig: DMAConfig)
    extends IOBus[AXI4](dmaConfig) {
  val io = IO(new Bundle {
    val bus = new AXI4(addrWidth, dataWidth)

    val dataIO = DeqIO(UInt(dataWidth.W))

    val xfer = Flipped(new XferDescBundle(addrWidth))
  })

  val sDataIdle :: sDataTransfer :: sDataResp :: sDataDone :: Nil = Enum(4)
  val sAddrIdle :: sAddrTransfer :: sAddrDone :: Nil = Enum(3)

  val dataState = RegInit(sDataIdle)
  val addrState = RegInit(sAddrIdle)

  val done = RegInit(false.B)
  val enable = RegInit(false.B)
  val last = WireInit(false.B)
  val length = RegInit(0.U(addrWidth.W))
  val awlen = RegInit(0.U(addrWidth.W))
  val awaddr = RegInit(0.U(addrWidth.W))
  val awsize = WireInit(log2Ceil(dataWidth / 8).U)
  val wstrb = WireInit(~0.U((dataWidth / 8).W))

  val awvalid = RegInit(false.B)
  val bready = RegInit(false.B)

  val ready = WireInit(io.bus.w.wready && enable)
  val valid = WireInit(io.dataIO.valid && enable)

  io.bus.aw <> AXI4AW(awaddr, awlen, awsize, awvalid)
  io.bus.w <> AXI4W(io.dataIO.bits, wstrb.asUInt, last, valid)
  io.bus.b <> AXI4B(bready)
  io.bus.ar <> AXI4AR.tieOff(addrWidth)
  io.bus.r <> AXI4R.tieOff(dataWidth)

  io.dataIO.ready := ready

  io.xfer.done := done

  last := length === 1.U

  switch(dataState) {
    is(sDataIdle) {
      done := false.B
      when(io.xfer.valid) {
        length := io.xfer.length
        dataState := sDataTransfer
        enable := true.B
      }
    }
    is(sDataTransfer) {
      when(ready && valid) {
        when(length > 1.U) {
          length := length - 1.U
        }.otherwise {
          dataState := sDataResp
          enable := false.B
          bready := true.B
        }
      }
    }
    is(sDataResp) {
      when(bready && io.bus.b.bvalid) {
        bready := false.B
        dataState := sDataDone
      }
    }
    is(sDataDone) {
      done := true.B
      dataState := sDataIdle
    }
  }

  switch(addrState) {
    is(sAddrIdle) {
      when(io.xfer.valid) {
        awaddr := io.xfer.address
        awlen := io.xfer.length - 1.U
        awvalid := true.B
        addrState := sAddrTransfer
      }
    }
    is(sAddrTransfer) {
      when(awvalid && io.bus.aw.awready) {
        addrState := sAddrDone
        awvalid := false.B
      }
    }
    is(sAddrDone) {
      when(done) {
        addrState := sAddrIdle
      }
    }
  }
}
