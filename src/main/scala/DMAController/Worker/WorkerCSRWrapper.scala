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

import DMAController.CSR._
import DMAController.DMATop
import chisel3._
import chisel3.util.Cat

class WorkerCSRWrapper(addrWidth : Int, readerDataWidth : Int, writerDataWidth : Int, readerMaxBurst : Int,
                       writerMaxBurst : Int, reader4KBarrier : Boolean, writer4KBarrier : Boolean) extends Module{
  val io = IO(new Bundle{
    val csr: Vec[CSRRegBundle] = Vec(DMATop.controlRegCount, Flipped(new CSRRegBundle()))
    val irq = new InterruptBundle
    val sync = new SyncBundle
    val xferRead = new XferDescBundle(addrWidth)
    val xferWrite = new XferDescBundle(addrWidth)
  })

  val addressGeneratorRead = Module(new AddressGenerator(addrWidth, readerDataWidth))
  val transferSplitterRead = Module(new TransferSplitter(addrWidth, readerDataWidth, readerMaxBurst, reader4KBarrier))

  val addressGeneratorWrite = Module(new AddressGenerator(addrWidth, writerDataWidth))
  val transferSplitterWrite = Module(new TransferSplitter(addrWidth, writerDataWidth, writerMaxBurst, writer4KBarrier))

  val status = RegNext(Cat(addressGeneratorRead.io.ctl.busy, addressGeneratorWrite.io.ctl.busy))

  val readerSync = RegNext(io.sync.readerSync)
  val readerSyncOld = RegNext(readerSync)

  val writerSync = RegNext(io.sync.writerSync)
  val writerSyncOld = RegNext(writerSync)

  val readerStart = RegInit(false.B)
  val writerStart = RegInit(false.B)

  val control = Wire(UInt())
  val clear = WireInit(Cat(readerStart, writerStart))

  control := ClearCSR(clear, io.csr(0))

  StatusCSR(status, io.csr(1))

  io.irq <> InterruptController(addressGeneratorRead.io.ctl.busy, addressGeneratorWrite.io.ctl.busy,
    io.csr(2), io.csr(3))

  readerStart := ((!readerSyncOld && readerSync) || control(3)) && control(1)
  writerStart := ((!writerSyncOld && writerSync) || control(2)) && control(0)

  addressGeneratorRead.io.ctl.start := readerStart
  addressGeneratorRead.io.ctl.startAddress := SimpleCSR(io.csr(4))
  addressGeneratorRead.io.ctl.lineLength := SimpleCSR(io.csr(5))
  addressGeneratorRead.io.ctl.lineCount := SimpleCSR(io.csr(6))
  addressGeneratorRead.io.ctl.lineGap := SimpleCSR(io.csr(7))

  addressGeneratorWrite.io.ctl.start := writerStart
  addressGeneratorWrite.io.ctl.startAddress := SimpleCSR(io.csr(8))
  addressGeneratorWrite.io.ctl.lineLength := SimpleCSR(io.csr(9))
  addressGeneratorWrite.io.ctl.lineCount := SimpleCSR(io.csr(10))
  addressGeneratorWrite.io.ctl.lineGap := SimpleCSR(io.csr(11))

  for(i <- 12 until DMATop.controlRegCount){
    SimpleCSR(io.csr(i))
  }

  transferSplitterRead.io.xferIn <> addressGeneratorRead.io.xfer
  io.xferRead <> transferSplitterRead.io.xferOut

  transferSplitterWrite.io.xferIn <> addressGeneratorWrite.io.xfer
  io.xferWrite <> transferSplitterWrite.io.xferOut

}
