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
import chisel3.util._

class WishboneSlave(val addrWidth : Int, val dataWidth : Int) extends Bundle{
  /* data */
  val dat_i = Input(UInt(dataWidth.W))
  val dat_o = Output(UInt(dataWidth.W))
  /* control */
  val cyc_i = Input(Bool())
  val stb_i = Input(Bool())
  val we_i = Input(Bool())
  val adr_i = Input(UInt((addrWidth - log2Ceil(dataWidth / 8)).W))
  val sel_i = Input(UInt((dataWidth / 8).W))
  val ack_o = Output(Bool())
  val stall_o = Output(Bool())
  val err_o = Output(Bool())
}
