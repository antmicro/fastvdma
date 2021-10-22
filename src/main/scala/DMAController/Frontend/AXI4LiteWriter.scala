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

import DMAController.Bus._
import DMAController.Worker.XferDescBundle
import chisel3._
import chisel3.util._

class AXI4LiteWriter(val addrWidth : Int, val dataWidth : Int) extends Module{
  val io = IO(new Bundle{
    val bus = new AXI4Lite(addrWidth, dataWidth)

    val dataIn = DeqIO(UInt(dataWidth.W))

    val xfer = Flipped(new XferDescBundle(addrWidth))
  })

  val sDataIdle :: sDataTransfer :: sDataResp :: sDataDone :: Nil = Enum(4)
  val sAddrIdle :: sAddrTransfer :: sAddrDone :: Nil = Enum(3)

  val dataState = RegInit(sDataIdle)
  val addrState = RegInit(sAddrIdle)

  val done = RegInit(false.B)
  val enable = RegInit(false.B)
  val awaddr = RegInit(0.U(addrWidth.W))
  val wstrb = WireInit(~0.U((dataWidth/8).W))


  val awvalid = RegInit(false.B)
  val bready = RegInit(false.B)

  val ready = WireInit(io.bus.w.wready && enable)
  val valid = WireInit(io.dataIn.valid && enable)

  io.bus.aw <> AXI4LAW(awaddr, awvalid)
  io.bus.w <> AXI4LW(io.dataIn.bits, wstrb.asUInt, valid)
  io.bus.b <> AXI4LB(bready)
  io.bus.ar <> AXI4LAR.tieOff(addrWidth)
  io.bus.r <> AXI4LR.tieOff(dataWidth)

  io.dataIn.ready := ready

  io.xfer.done := done

  switch(dataState){
    is(sDataIdle){
      done := false.B
      when(io.xfer.valid){
        dataState := sDataTransfer
        enable := true.B
      }
    }
    is(sDataTransfer){
      when(ready && valid){
        dataState := sDataResp
        enable := false.B
        bready := true.B
      }
    }
    is(sDataResp){
      when(bready && io.bus.b.bvalid){
        bready := false.B
        dataState := sDataDone
      }
    }
    is(sDataDone){
      done := true.B
      dataState := sDataIdle
    }
  }

  switch(addrState){
    is(sAddrIdle){
      when(io.xfer.valid){
        awaddr := io.xfer.address
        awvalid := true.B
        addrState := sAddrTransfer
      }
    }
    is(sAddrTransfer){
      when(awvalid && io.bus.aw.awready){
        addrState := sAddrDone
        awvalid := false.B
      }
    }
    is(sAddrDone){
      when(done){
        addrState := sAddrIdle
      }
    }
  }
}
