# Simulation

## Chisel test simulation

FastVDMA can be simulated as a whole but certain components can be tested separately.

You can simulate the full memory to memory design by running:

```bash
make testM2M
```

The full stream to memory test by:

```
make testS2M
```

To run both full tests:

```bash
make test
```

To run all tests, including all mentioned above, execute:

```bash
make testall
```

Each test run generates a `.vcd` file which can be opened using GTKWave or any other `.vcd` viewer.

Output files are located in a separate sub directories inside the `test_run_dir` directory.

The full test should generate an `outM2M.png/outS2M.png` file demonstrating a 2D transfer with configurable stride.
The resulting image should look similar to:

![Reference image](ref-output.png)

## Renode-Verilator co-simulation

FastVDMA provided [script](https://github.com/renode/renode/blob/master/scripts/single-node/zynq_verilated_fastvdma.resc) for the open source software development framework - [Renode](https://github.com/renode/renode/tree/master).

With the use of [renode-verilator-integration](https://github.com/antmicro/renode-verilator-integration/tree/master), Renode runs Verilator simulation of the FastVDMA design and simulates the rest of the environment itself. This includes booting the Linux kernel. You may read more about the Renode's co-simulation feature from [this blog note](https://antmicro.com/blog/2021/09/co-simulation-for-zynq-with-renode-and-verilator/).

Then, loading the [FastVDMA driver](https://github.com/antmicro/linux-xlnx/tree/fastvdma-driver/drivers/dma/fastvdma) it performs a demo image transfer, similar to the one from chisel test. FastVDMA provides the source code to the example driver that uses FastVDMA driver through DMAEngine as well as the [demo user-space application](https://github.com/antmicro/linux-xlnx/tree/fastvdma-demo/drivers/dma/fastvdma).