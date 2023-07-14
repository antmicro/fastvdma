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

Supported buses
---------------

- Data
  - AXI4
  - AXI-Stream
  - Wishbone
- Control
  - AXI4-Lite
  - Wishbone

Dependencies
------------

Because the controller is written in Chisel, it requires `sbt`, `scala` and `java` to be installed; additionally the tests require `imagemagick`.

Simulation
----------

FastVDMA can be simulated as a whole but certain components can be tested separately.

You can simulate the full memory to memory design by running:

`make testM2M`

And the full stream to memory test by:

`make testS2M`

Each test run generates a `.vcd` file which can be opened using GTKWave or any other `.vcd` viewer.
Output files are located in a separate sub directories inside the `test_run_dir` directory.

The full test should generate an `outM2M.png/outS2M.png` file demonstrating a 2D transfer with configurable stride. The resulting image should look similar to:

![Reference image](docs/ref-output.png)

Synthesis
---------

To generate a synthesizable verilog file either proceed with the default configuration by running:

`make verilog`

Or provide a valid configuration file with:

`make CONFIG_FILE=<path_to_json_file> verilog`

The generated file will be named `DMATop$(configuration).v` where `configuration` is chosen configuration of buses in the DMA. Verilog module will be named in the same manner.
See the documentation on how to provide custom configuration -- link here.

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
