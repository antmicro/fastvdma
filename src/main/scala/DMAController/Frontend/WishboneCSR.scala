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

import DMAController.Bus.WishboneSlave
import DMAController.CSR.{CSR, CSRBusBundle}
import DMAController.DMATop
import chisel3._
import chisel3.util._

class WishboneCSR(addrWidth : Int) extends Module{
  val io = IO(new Bundle {
    val ctl = new WishboneSlave(addrWidth, DMATop.controlDataWidth)
    val bus = new CSRBusBundle
  })

  val sIdle :: sAck :: Nil = Enum(2)

  val state = RegInit(sIdle)

  val ack  = RegInit(false.B)

  val valid = WireInit(io.ctl.stb_i & io.ctl.cyc_i)

  switch(state){
    is(sIdle){
      ack := false.B
      when(io.ctl.stb_i & io.ctl.cyc_i){
        state := sAck
        ack := true.B
      }
    }
    is(sAck){
      ack := false.B
      state := sIdle
    }
  }

  io.ctl.stall_o := false.B
  io.ctl.err_o := false.B

  io.ctl.ack_o := ack
  io.bus.write := ack & io.ctl.we_i
  io.bus.read := ack & !io.ctl.we_i

  io.bus.dataOut := io.ctl.dat_i
  io.ctl.dat_o := io.bus.dataIn
  io.bus.addr := io.ctl.adr_i
}
