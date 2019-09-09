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

import chisel3.iotesters._

class InterruptControllerTest(dut : InterruptController) extends PeekPokeTester(dut){
  poke(dut.io.writeBusy, 0)
  poke(dut.io.readBusy, 0)

  poke(dut.io.imr.dataRead, 0)
  poke(dut.io.imr.dataWrite, 0)

  poke(dut.io.isr.dataRead, 0)
  poke(dut.io.isr.dataWrite, 0)

  step(5)

  poke(dut.io.imr.dataOut, 0x3)
  poke(dut.io.imr.dataWrite, 1)

  step(1)

  poke(dut.io.imr.dataWrite, 0)

  step(1)

  poke(dut.io.writeBusy, 1)
  poke(dut.io.readBusy, 1)

  step(5)

  poke(dut.io.writeBusy, 1)
  poke(dut.io.readBusy, 0)

  step(5)

  poke(dut.io.writeBusy, 0)
  poke(dut.io.readBusy, 0)

  step(5)

  poke(dut.io.isr.dataOut, 0x3)
  poke(dut.io.isr.dataWrite, 1)

  step(1)

  poke(dut.io.isr.dataWrite, 0)

  step(5)

}
