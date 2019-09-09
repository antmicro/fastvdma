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
package DMAController.CSR

import DMAController.DMATop
import chisel3._

class CSR(val addrWidth : Int) extends Module{
  val io = IO(new Bundle{
    val csr: Vec[CSRRegBundle] = Vec(DMATop.controlRegCount, new CSRRegBundle())
    val bus = Flipped(new CSRBusBundle)
  })

  val data = WireInit(0.U(DMATop.controlDataWidth.W))

  io.bus.dataIn := data

  for(i <- 0 until DMATop.controlRegCount){
    when(io.bus.addr === i.U && io.bus.read){
      data := io.csr(i).dataIn
      io.csr(i).dataRead := true.B
    }.otherwise{
      io.csr(i).dataRead := false.B
    }

    when(io.bus.addr === i.U && io.bus.write){
      io.csr(i).dataOut := io.bus.dataOut
      io.csr(i).dataWrite := true.B
    }.otherwise{
      io.csr(i).dataWrite := false.B
      io.csr(i).dataOut := 0.U
    }
  }
}