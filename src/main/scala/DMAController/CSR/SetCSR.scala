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

class SetCSR extends Module{
  val io = IO(new Bundle{
    val csr = Flipped(new CSRRegBundle())
    val value = Output(UInt(DMATop.controlDataWidth.W))
    val set = Input(UInt(DMATop.controlDataWidth.W))
  })

  val reg = RegInit(0.U(DMATop.controlDataWidth.W))

  io.csr.dataIn := reg
  io.value := reg

  when(io.csr.dataWrite){
    reg := (reg & (~io.csr.dataOut).asUInt) | io.set
  }.otherwise{
    reg := reg | io.set
  }

}

object SetCSR {
  def apply(set : UInt, csrCtl : CSRRegBundle): UInt = {
    val csr = Module(new SetCSR())

    csr.io.set := set

    csr.io.csr <> csrCtl

    csr.io.value
  }
}

