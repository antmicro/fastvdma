/*
MIT License

Copyright (c) 2019 Antmicro

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
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
