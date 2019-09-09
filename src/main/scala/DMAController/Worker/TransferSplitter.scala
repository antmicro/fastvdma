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

class TransferSplitter(val addressWidth : Int, val dataWidth : Int,
                       val maxLength : Int, val canCrossBarrier : Boolean) extends Module{
  val io = IO(new Bundle{
    val xferIn = Flipped(new XferDescBundle(addressWidth))
    val xferOut = new XferDescBundle(addressWidth)
  })

  if(maxLength != 0) {
    val sIdle :: sSplit :: sSplitWait :: Nil = Enum(3)

    val dataBytes: Int = dataWidth / 8

    val barrierBytes: Int = 4096

    val address_i = RegInit(0.U(addressWidth.W))
    val length_i = RegInit(0.U(addressWidth.W))

    val address_o = RegInit(0.U(addressWidth.W))
    val length_o = RegInit(0.U(addressWidth.W))

    val done = RegInit(false.B)
    val valid = RegInit(false.B)

    val state = RegInit(sIdle)

    io.xferIn.done := done
    io.xferOut.valid := valid

    io.xferOut.address := address_o
    io.xferOut.length := length_o

    switch(state) {
      is(sIdle) {
        done := false.B

        when(io.xferIn.valid) {
          address_i := io.xferIn.address
          length_i := io.xferIn.length
          state := sSplit
        }
      }
      is(sSplit) {
        address_o := address_i
        valid := true.B
        state := sSplitWait

        when(length_i > maxLength.U) {
          if (canCrossBarrier) {
            length_o := maxLength.U
            length_i := length_i - maxLength.U
            address_i := address_i + maxLength.U * dataBytes.U
          } else {
            val bytesToBarrier = barrierBytes.U - address_i % barrierBytes.U
            when(bytesToBarrier < maxLength.U * dataBytes.U) {
              length_o := bytesToBarrier / dataBytes.U
              length_i := length_i - bytesToBarrier / dataBytes.U
              address_i := address_i + bytesToBarrier
            }.otherwise {
              length_o := maxLength.U
              length_i := length_i - maxLength.U
              address_i := address_i + maxLength.U * dataBytes.U
            }
          }

        }.otherwise {
          if (canCrossBarrier) {
            length_o := length_i
            length_i := 0.U
            address_i := address_i + length_i * dataBytes.U
          } else {
            val bytesToBarrier = barrierBytes.U - address_i % barrierBytes.U
            when(bytesToBarrier < length_i * dataBytes.U) {
              length_o := bytesToBarrier / dataBytes.U
              length_i := length_i - bytesToBarrier / dataBytes.U
              address_i := address_i + bytesToBarrier
            }.otherwise {
              length_o := length_i
              length_i := 0.U
              address_i := address_i + length_i * dataBytes.U
            }
          }
        }
      }
      is(sSplitWait) {
        valid := false.B
        when(io.xferOut.done) {
          when(length_i > 0.U) {
            state := sSplit
          }.otherwise {
            state := sIdle
            done := true.B
          }
        }
      }
    }
  } else {
    io.xferOut <> io.xferIn
  }
}
