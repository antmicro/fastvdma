Fast Versatile DMA
==================

Copyright (c) 2019-2021 [Antmicro](https://www.antmicro.com)

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

You can simulate the full design by running:

`make test`

To run all tests, including the full test mentioned above, execute:

`make testall`

Each testrun generates a `.vcd` file which can be opened using GTKWave or any other `.vcd` viewer.
Output files are located in a separate subdirectories inside the `test_run_dir` directory.

The full test should generate an `out.png` file demonstrating a 2D transfer with configurable stride. The resulting image should look similar to: 

![Reference image](doc/ref-output.png)

Synthesis
---------

To generate a synthesizable verilog file, run:

`make verilog`

The generated file will be named `DMATop.v`

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

Customizing FastVDMA
--------------------

Configuration for the DMA is located in the [DMATop](src/main/scala/DMAController/DMATop.scala) file.
Most of the settings are defined in the `DMATop` companion object but to change which busses are used, the `DMATop` class must be modified to contain correct `io` bundles and `*Frontend` modules.
After making changes to interfaces used in `DMATop` class make sure to verify that companion object is correctly configured.

Source code structure
---------------------
- [src/main/scala/DMAController](src/main/scala/DMAController) contains sources of the DMA controller
  - [Bus](src/main/scala/DMAController/Bus) contains definitions of various bus bundles
  - [CSR](src/main/scala/DMAController/CSR) contains code responsible for handling configuration registers
  - [Frontend](src/main/scala/DMAController/Frontend) contains modules handling various bus types
  - [Worker](src/main/scala/DMAController/Worker) contains generic code supporting controlling the DMA behaviour
- [src/test/scala/DMAController](src/test/scala/DMAController) contains tests
  - [Bfm](src/test/scala/DMAController/Bfm) contains Bus models that are used in full configuration tests
  - [Frontend](src/test/scala/DMAController/Frontend) contains tests used for generating timing diagrams for various bus types
  - [Worker](src/test/scala/DMAController/Worker) contains tests that generate timinig diagrams for the generic part of the DMA

Linux drivers
-------------

FastVDMA can be controlled using a Linux driver.
The source code and relevant documentation can be found in a [separate repository](https://github.com/antmicro/linux-xlnx/tree/fastvdma-driver).
