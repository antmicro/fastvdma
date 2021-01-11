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
