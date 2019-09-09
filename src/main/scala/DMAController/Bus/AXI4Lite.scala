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
package DMAController.Bus

import chisel3._

class AXI4LAW(val addrWidth : Int) extends Bundle {
  val awaddr = Output(UInt(addrWidth.W))
  val awprot = Output(UInt(AXI4Lite.protWidth.W))
  val awvalid = Output(Bool())
  val awready = Input(Bool())
}

object AXI4LAW {
  def apply(addr : UInt, valid : UInt) : AXI4LAW = {
    val aw = Wire(new AXI4LAW(addr.getWidth))
    aw.awprot := 0.U
    aw.awaddr := addr
    aw.awvalid := valid
    aw
  }
}

class AXI4LW(val dataWidth : Int) extends Bundle {
  val wdata = Output(UInt(dataWidth.W))
  val wstrb = Output(UInt((dataWidth/8).W))
  val wvalid = Output(Bool())
  val wready = Input(Bool())
}

object AXI4LW {
  def apply(data : UInt, strb : UInt, valid : UInt) : AXI4LW = {
    val w = Wire(new AXI4LW(data.getWidth))
    w.wdata := data
    w.wstrb := strb
    w.wvalid := valid
    w
  }
}

class AXI4LB extends Bundle {
  val bresp = Input(UInt(AXI4Lite.respWidth.W))
  val bvalid = Input(Bool())
  val bready = Output(Bool())
}

object AXI4LB{
  def apply(ready : UInt): AXI4LB = {
    val b = Wire(new AXI4LB())
    b.bready := ready
    b
  }
}

class AXI4LAR(val addrWidth : Int) extends Bundle {
  val araddr = Output(UInt(addrWidth.W))
  val arprot = Output(UInt(AXI4Lite.protWidth.W))
  val arvalid = Output(Bool())
  val arready = Input(Bool())
}

object AXI4LAR {
  def apply(addr : UInt, valid : UInt) : AXI4LAR = {
    val ar = Wire(new AXI4LAR(addr.getWidth))
    ar.arprot := 0.U
    ar.araddr := addr
    ar.arvalid := valid
    ar
  }
  def tieOff(addrWidth : Int) : AXI4LAR = {
    val ar = Wire(new AXI4LAR(addrWidth))
    ar.arprot := 0.U
    ar.araddr := 0.U
    ar.arvalid := 0.U
    ar
  }
}

class AXI4LR(val dataWidth : Int) extends Bundle {
  val rdata = Input(UInt(dataWidth.W))
  val rresp = Input(UInt(AXI4Lite.respWidth.W))
  val rvalid = Input(Bool())
  val rready = Output(Bool())
}

object AXI4LR {
  def apply(dataWidth : Int, ready : UInt) : AXI4LR = {
    val r = Wire(new AXI4LR(dataWidth))
    r.rready := ready
    r
  }
  def tieOff(dataWidth : Int) : AXI4LR = {
    val r = Wire(new AXI4LR(dataWidth))
    r.rready := 0.U
    r
  }
}

class AXI4Lite(val addrWidth : Int, val dataWidth : Int) extends Bundle{
  val aw = new AXI4LAW(addrWidth)
  val w = new AXI4LW(dataWidth)
  val b = new AXI4LB()
  val ar = new AXI4LAR(addrWidth)
  val r = new AXI4LR(dataWidth)
}

object AXI4Lite {
  val protWidth = 3
  val respWidth = 2
}
