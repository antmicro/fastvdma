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

import DMAController.Bus.WishboneMaster
import DMAController.Worker.XferDescBundle
import chisel3._
import chisel3.util._

class WishboneClassicPipelinedWriter(val addrWidth : Int, val dataWidth : Int) extends Module{
  val io = IO(new Bundle{
    val bus = new WishboneMaster(addrWidth, dataWidth)
    val dataIn = DeqIO(UInt(dataWidth.W))
    val xfer = Flipped(new XferDescBundle(addrWidth))
  })

  val sIdle :: sWait :: Nil = Enum(2)

  val state = RegInit(sIdle)

  val valid = WireInit(io.dataIn.valid)

  val stbCnt = RegInit(0.U(addrWidth.W))
  val ackCnt = RegInit(0.U(addrWidth.W))
  val adr = RegInit(0.U(addrWidth.W))
  val cyc = WireInit(ackCnt =/= 0.U)
  val stb = WireInit(stbCnt =/= 0.U && valid)

  val ready = WireInit(cyc && stb && !io.bus.stall_i)

  val done = RegInit(false.B)

  io.bus.dat_o := io.dataIn.bits
  io.dataIn.ready := ready


  io.bus.we_o := true.B
  io.bus.sel_o := ~0.U((dataWidth / 8).W)
  io.bus.adr_o := adr
  io.bus.cyc_o := cyc
  io.bus.stb_o := stb

  io.xfer.done := done

  switch(state){
    is(sIdle){
      done := false.B
      when(io.xfer.valid) {
        state := sWait
        stbCnt := io.xfer.length
        ackCnt := io.xfer.length
        adr := io.xfer.address
      }
    }
    is(sWait){
      when(ackCnt === 0.U && stbCnt === 0.U){
        state := sIdle
        done := true.B
      }
    }
  }

  when(ackCnt =/= 0.U && io.bus.ack_i){
    ackCnt := ackCnt - 1.U
  }

  when(stbCnt =/= 0.U && ready){
    adr := adr + (dataWidth / 8).U
    stbCnt := stbCnt - 1.U
  }

}
