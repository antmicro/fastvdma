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

import DMAController.CSR.{CSRRegBundle, SetCSR, SimpleCSR}
import chisel3._
import chisel3.util.Cat

class InterruptController extends Module{
  val io = IO(new Bundle {
    val irq = new InterruptBundle
    val readBusy = Input(Bool())
    val writeBusy = Input(Bool())
    val imr = Flipped(new CSRRegBundle())
    val isr = Flipped(new CSRRegBundle())
  })

  val mask = WireInit(SimpleCSR(io.imr))

  val readBusy = RegNext(io.readBusy)
  val readBusyOld = RegNext(readBusy)

  val writeBusy = RegNext(io.writeBusy)
  val writeBusyOld = RegNext(writeBusy)

  val writeBusyIrq = RegInit(false.B)
  val readBusyIrq = RegInit(false.B)

  writeBusyIrq := writeBusyOld && !writeBusy && mask(0)
  readBusyIrq := readBusyOld && !readBusy && mask(1)

  val irq = WireInit(Cat(readBusyIrq, writeBusyIrq))

  val isr = WireInit(SetCSR(irq, io.isr))

  io.irq.writerDone := isr(0)
  io.irq.readerDone := isr(1)
}

object InterruptController {
  def apply(readBusy : Bool, writeBusy : Bool, imr : CSRRegBundle, isr : CSRRegBundle): InterruptBundle = {
    val irqc = Module(new InterruptController)

    irqc.io.readBusy := readBusy
    irqc.io.writeBusy := writeBusy

    irqc.io.imr <> imr
    irqc.io.isr <> isr

    irqc.io.irq
  }
}