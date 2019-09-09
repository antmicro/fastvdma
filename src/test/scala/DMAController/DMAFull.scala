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
package DMAController

import scala.reflect.runtime.universe._
import DMAController.Bfm._
import chisel3.iotesters._
import chisel3._

class DMAFull(dut: DMATop) extends PeekPokeTester(dut){
  val width = 256
  val height = 256
  val min = 0
  val max = width * height * 2
  
  var cnt: Int = 0

  def waitRange(data: Bits, exp: Int, min: Int, max: Int) : Unit = {
    var cnt = 0

    while(peek(data) != exp && cnt < max){
      step(1)
      cnt += 1
    }

    assert(cnt < max)
    assert(cnt >= min)
  }

  val cls = runtimeMirror(getClass.getClassLoader).reflect(this)
  val members = cls.symbol.typeSignature.members

  def bfms = members.filter(_.typeSignature <:< typeOf[ChiselBfm])

  def stepSingle(): Unit = {
    for(bfm <- bfms){
      cls.reflectField(bfm.asTerm).get.asInstanceOf[ChiselBfm].update(cnt)
    }
    super.step(1)
  }

  override def step(n: Int): Unit = {
    for(_ <- 0 until n) {
      stepSingle
    }
  }

  val axil_master = new AxiLiteMasterBfm(dut.io.control, peek, poke, println)
  val axis_master = new AxiStreamMasterBfm(dut.io.read, width, peek, poke, println)
  val axi4_slave = new Axi4SlaveBfm(dut.io.write, width * height, peek, poke, println)

  axis_master.loadFromFile("./img0.rgba")
  axi4_slave.loadFromFile("./img1.rgba")

  axil_master.writePush(0x10, 0)
  axil_master.writePush(0x14, width)
  axil_master.writePush(0x18, height)
  axil_master.writePush(0x1c, 0)

  axil_master.writePush(0x20, height * width * 4 + width * 2)
  axil_master.writePush(0x24, width)
  axil_master.writePush(0x28, height)
  axil_master.writePush(0x2c, width)

  step(100)

  axil_master.writePush(0x08, 3)

  axil_master.writePush(0x00, 0xf)

  waitRange(dut.io.irq.writerDone, 1, min, max)

  expect(dut.io.irq.writerDone, 1)
  expect(dut.io.irq.readerDone, 1)

  axi4_slave.saveToFile("./out.rgba")
}
