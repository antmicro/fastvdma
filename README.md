Fast Versatile DMA
==================

Copyright (c) 2019-2023 [Antmicro](https://www.antmicro.com)

Overview
--------

FastVDMA is a DMA controller designed with portability and customizability in mind.

Supported features
------------------

- Interrupts
- 2D transfers with configurable stride
- External frame synchronization inputs

Supported busses
----------------

- Data
  - AXI4
  - AXI-Stream
  - Wishbone
- Control
  - AXI4-Lite
  - Wishbone

Performance
-----------

FastVDMA performance was tested in synthetic tests that consisted of transferring an `NxM` buffer with data where `N` represents the number of 32-bit words and `M` represents the number of `N` word rows to transfer.

FastVDMA was verified in the `xc7z030fbg676-2` chip achieving an average throughput of 750MB/s, while being clocked at 250MHz, and average of 330MB/s at 100MHz under the same workload. Both the speeds were performed in a Memory-Stream-Memory configuration using two controllers configured with AXI4 and AXI-Stream buses. The first controller reads data from memory and sends it out via an AXI-Stream interface, while the second receives the stream and writes the data received to a second buffer in memory.

Wishbone and AXI4 busses were connected to a [LiteDRAM](https://github.com/enjoy-digital/litedram) controller providing access to DDR3 memory.
Both busses used a 32-bit data bus to connect to the DDR3 controller.

In both cases the data transferred consisted of a 4MB block of randomly produced data which was subsequently verified for possible transmission errors after each transfer.

Resource usage
--------------

The AXI4=\>AXI-Stream (MM2S) configuration utilized 425 slices on a `xc7z030fbg676-2` chip which was used for tesing the design.
AXI-Stream=\>AXI4 (S2MM) requires 455 slices on the same chip.
Both configurations were instantiated in the same design and connected in a back-to-back configuration that allowed memory-to-memory transfers while still using configurations equipped with AXI-Stream interfaces.

Dependencies
------------

Because the controller is written in Chisel, it requires `sbt`, `scala` and `java` to be installed; additionally the tests require `imagemagick`.

Simulation
----------

FastVDMA can be simulated as a whole but certain components can be tested separately.

You can simulate the full memory to memory design by running:

`make testM2M`

The full stream to memory test by:

`make testS2M`

To run both full tests:

`make test`

To run all tests, including all mentioned above, execute:

`make testall`

Each testrun generates a `.vcd` file which can be opened using GTKWave or any other `.vcd` viewer.
Output files are located in a separate subdirectories inside the `test_run_dir` directory.

The full test should generate an `outM2M.png/outS2M.png` file demonstrating a 2D transfer with configurable stride. The resulting image should look similar to:

![Reference image](doc/ref-output.png)

Synthesis
---------

To generate a synthesizable verilog file either proceeed with the default configuration by running:

`make verilog`

Or provide a valid configuration file with:

`make CONFIG_FILE=<path_to_json_file> verilog`

The generated file will be named `DMATop$(configuration).v` where `configuration` is chosen configuration of buses in the DMA. Verilog module will be named in the same manner.

Configuration file
---------

A configuration file should be written in json. Here's an example configuration:
```
{
    "configuration": "AXI_AXIL_AXI",
    "addrWidth": 32,
    "readDataWidth": 32,
    "writeDataWidth": 32,
    "readMaxBurst": 0,
    "writeMaxBurst": 16,
    "reader4KBarrier": false,
    "writer4KBarrier": true,
    "controlDataWidth": 32,
    "controlAddrWidth": 32,
    "controlRegCount": 16,
    "fifoDepth": 512
}
```

The `configuration` field specifies the choice of buses for data transfer (the 1st and 3rd element) and CSR handling (the 2nd element).

List of all supported DMA bus configurations is available in the [DMAConfig](src/main/scala/DMAController/DMAConfig.scala#51) file.

Register map
------------

Current register layout is shown in the table below:

|Address | Role                       |
|--------|----------------------------|
|`0x00`  |Control register            |
|`0x04`  |Status register             |
|`0x08`  |Interrupt mask regiser      |
|`0x0c`  |Interrupt status register   |
|`0x10`  |Reader start address        |
|`0x14`  |Reader line length          |
|`0x18`  |Reader line count           |
|`0x1c`  |Reader stride between lines |
|`0x20`  |Writer start address        |
|`0x24`  |Writer line length          |
|`0x28`  |Writer line count           |
|`0x2c`  |Writer stride between lines |
|`0x30`  |Version register            |
|`0x34`  |Configuration register      |

For a detailed description of register fields check [Register fields](doc/csr.md).

You can also check [WorkerCSRWrapper](src/main/scala/DMAController/Worker/WorkerCSRWrapper.scala) for more details on how the CSRs are attached to the DMA logic (`io.csr(0)` refers to `0x00`, `io.csr(1)` to `0x04` and so on).

Tests on customized FastVDMA
--------------------

If you would like to reuse provided tests to test your custom model you will need to write a test file similar to [DMAFullMem](src/test/scala/DMAController/DMAFullMem.scala).

What you need to alter is:
- Cast buses in the `io` field accordingly to chosen configuration. Example for `AXIS_AXIL_AXI` bus configuration:

```
val io = dut.io.asInstanceOf[Bundle{
                                val control: AXI4Lite
                                val read: AXIStream
                                val write: AXI4
                                val irq: InterruptBundle
                                val sync: SyncBundle}]
```

- You will also need to remember to provide correct BFMs for the test:

```
  val control = new AxiLiteMasterBfm(io.control, peek, poke, println)
  val reader = new AxiStreamMasterBfm(io.read, width, peek, poke, println)
  val writer = new Axi4SlaveBfm(io.write, width * height, peek, poke, println)
```

- Lastly, add an entry in [ControllerSpec](src/test/scala/DMAController/ControllerSpec.scala) (or write your Tester):

```
val myConfiguration = new DMAConfig(...)
it should "perform image transfer with my custom configuration" in {
    test(new DMATop(myConfiguration)).runPeekPoke(dut =>
      new ImageTransfer(dut, new <CustomTestClassName>(dut), myConfiguration)
    )
  }
```

After successful test, the image `out$(configuration).rgba` will be produced (where `configuration` is the DMA bus configuration).

Source code structure
---------------------
- [src/main/scala/DMAController](src/main/scala/DMAController) contains sources of the DMA controller
  - [Bus](src/main/scala/DMAController/Bus) contains definitions of various bus bundles
  - [CSR](src/main/scala/DMAController/CSR) contains code responsible for handling configuration registers
  - [Frontend](src/main/scala/DMAController/Frontend) contains modules handling various bus types
  - [Worker](src/main/scala/DMAController/Worker) contains generic code supporting controlling the DMA behavior
- [src/test/scala/DMAController](src/test/scala/DMAController) contains tests
  - [Bfm](src/test/scala/DMAController/Bfm) contains Bus models that are used in full configuration tests
  - [Frontend](src/test/scala/DMAController/Frontend) contains tests used for generating timing diagrams for various bus types
  - [Worker](src/test/scala/DMAController/Worker) contains tests that generate timing diagrams for the generic part of the DMA

Linux drivers
-------------

FastVDMA can be controlled using a Linux driver.
The source code and relevant documentation can be found in a [separate repository](https://github.com/antmicro/linux-xlnx/tree/fastvdma-driver).
