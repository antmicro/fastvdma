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

class TransferSplitterTest(dut : TransferSplitter) extends PeekPokeTester(dut){
  poke(dut.io.xferIn.address, 16)
  poke(dut.io.xferIn.length, 2048)
  poke(dut.io.xferIn.valid, 0)
  poke(dut.io.xferOut.done, 0)

  step(10)

  poke(dut.io.xferIn.valid, 1)
  step(1)
  poke(dut.io.xferIn.valid, 0)

  while(peek(dut.io.xferIn.done) == 0){
    step(10)
    poke(dut.io.xferOut.done, 1)
    step(1)
    poke(dut.io.xferOut.done, 0)
  }

}
