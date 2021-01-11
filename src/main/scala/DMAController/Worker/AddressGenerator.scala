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
package DMAController.Worker

import chisel3._
import chisel3.util._

class AddressGenerator(val addrWidth : Int, val dataWidth : Int) extends Module{
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

  when(state === sIdle){
    busy := false.B
  }.otherwise{
    busy := true.B
  }

  switch(state){
    is(sIdle){
      when(io.ctl.start){
        state := sLine

        address_i := io.ctl.startAddress
        length_i := io.ctl.lineLength
        lineCount := io.ctl.lineCount
        lineGap := io.ctl.lineGap
        first := true.B
      }
    }
    is(sLine){

      valid := true.B
      address_o := address_i
      length_o := length_i
      address_i := address_i + (length_i * (dataWidth / 8).U) + (lineGap * (dataWidth / 8).U)

      lineCount := lineCount - 1.U
      state := sLineWait
    }
    is(sLineWait){
      valid := false.B
      first := false.B
      when(io.xfer.done){
        when(lineCount > 0.U){
          state := sLine
        }.otherwise{
          state := sIdle
        }
      }
    }
  }

}
