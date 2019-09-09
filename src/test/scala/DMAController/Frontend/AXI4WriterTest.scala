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

class AXI4WriterTest(dut: AXI4Writer) extends PeekPokeTester(dut){
  val transferLength = 4
  var beatCount = 0

  poke(dut.io.bus.aw.awready, 0)
  poke(dut.io.bus.w.wready, 0)
  poke(dut.io.bus.b.bvalid, 0)

  poke(dut.io.dataIn.valid, 0)
  poke(dut.io.dataIn.bits, 0x55aa1234)

  poke(dut.io.xfer.address, 0x80000000)
  poke(dut.io.xfer.length, transferLength)
  poke(dut.io.xfer.valid, 0)

  step(5)

  poke(dut.io.xfer.valid, 1)

  step(1)

  poke(dut.io.xfer.valid, 0)

  step(1)

  poke(dut.io.bus.aw.awready, 1)

  while(peek(dut.io.bus.aw.awvalid) != 1){
    step(1)
  }

  step(1)

  poke(dut.io.bus.aw.awready, 0)

  step(1)

  poke(dut.io.dataIn.valid, 1)
  poke(dut.io.bus.w.wready, 1)

  while(peek(dut.io.bus.w.wlast) != 1){
    if(peek(dut.io.bus.w.wvalid) == 1){
      beatCount = beatCount + 1
    }
    step(1)
  }

  step(1)
  beatCount = beatCount + 1
  poke(dut.io.dataIn.valid, 0)
  poke(dut.io.bus.w.wready, 0)

  step(1)

  poke(dut.io.bus.b.bvalid, 1)

  while(peek(dut.io.bus.b.bready) != 1){
    step(1)
  }

  step(1)
  poke(dut.io.bus.b.bvalid, 0)

  while(peek(dut.io.xfer.done) != 1){
    step(1)
  }

  step(5)
}
