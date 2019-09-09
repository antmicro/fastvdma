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

import DMAController.Frontend._
import DMAController.Worker.{AddressGenerator, AddressGeneratorTest, InterruptController, InterruptControllerTest, TransferSplitter, TransferSplitterTest}
import org.scalatest.{FlatSpec, Matchers}

class ComponentSpec extends FlatSpec with Matchers{
  behavior of "ComponentSpec"

  it should "generate addresses" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new AddressGenerator(32, 32)) { dut =>
      new AddressGeneratorTest(dut)
    } should be(true)
  }
  it should "split transfers" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new TransferSplitter(32, 32, 256, false)) { dut =>
      new TransferSplitterTest(dut)
    } should be(true)
  }
  it should "perform AXI Stream master transfers" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new AXIStreamMaster(32, 32)) { dut =>
      new AXIStreamMasterTest(dut)
    } should be(true)
  }
  it should "perform AXI Stream slave transfers" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new AXIStreamSlave(32, 32)) { dut =>
      new AXIStreamSlaveTest(dut)
    } should be(true)
  }
  it should "perform AXI4 write transfers" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new AXI4Writer(32, 32)) { dut =>
      new AXI4WriterTest(dut)
    } should be(true)
  }
  it should "perform AXI4 read transfers" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new AXI4Reader(32, 32)) { dut =>
      new AXI4ReaderTest(dut)
    } should be(true)
  }
  it should "perform Wishbone write transfers" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new WishboneClassicPipelinedWriter(32, 32)) { dut =>
      new WishboneWriterTest(dut)
    } should be(true)
  }
  it should "trigger interrupts" in {
    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new InterruptController) { dut =>
      new InterruptControllerTest(dut)
    } should be(true)
  }
}
