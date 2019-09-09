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

import chisel3.iotesters._

class AXI4ReaderTest(dut: AXI4Reader) extends PeekPokeTester(dut){
  val transferLen = 4
  var currentLen = 0

  poke(dut.io.xfer.valid, 0)
  poke(dut.io.xfer.address, 0x40000000)
  poke(dut.io.xfer.length, transferLen)

  poke(dut.io.dataOut.ready, 0)

  poke(dut.io.bus.ar.arready, 0)

  poke(dut.io.bus.r.rvalid, 0)
  poke(dut.io.bus.r.rdata, 0x45671234)
  poke(dut.io.bus.r.rlast, 0)

  step(5)

  poke(dut.io.xfer.valid, 1)

  step(1)

  poke(dut.io.bus.ar.arready, 1)

  while(peek(dut.io.bus.ar.arvalid) != 1){
    printf("arvalid\n")
    step(1)
  }

  step(1)

  poke(dut.io.bus.ar.arready, 0)

  step(1)

  poke(dut.io.dataOut.ready, 1)
  poke(dut.io.bus.r.rvalid, 1)

  while(transferLen > (currentLen + 1)){
    printf("rready\n")
    if(peek(dut.io.bus.r.rready) == 1){
      currentLen += 1
    }
    step(1)
  }

  poke(dut.io.bus.r.rlast, 1)

  while(peek(dut.io.bus.r.rready) != 1){
    printf("rlast\n")
    step(1)
  }

  step(1)

  poke(dut.io.bus.r.rvalid, 0)
  poke(dut.io.bus.r.rlast, 0)

  while(peek(dut.io.xfer.done) != 1){
    printf("done")
    step(1)
  }

  step(5)

}
