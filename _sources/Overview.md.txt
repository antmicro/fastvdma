# Overview

FastVDMA is a DMA controller designed with portability and customizability in mind.

## Features

- Interrupts
- 2D transfers with configurable stride
- External frame synchronization inputs

---

## Supported buses

FastVDMA implements several most commonly used buses for data handling and CSR handling.

### Data

- AXI4
- AXI-Stream
- Wishbone

### Control

- AXI4-Lite
- Wishbone

---

## Performance

FastVDMA performance was tested in synthetic tests that consisted of transferring an `NxM` buffer with data where `N` represents the number of 32-bit words and `M` represents the number of `N` word rows to transfer.

FastVDMA was verified in the `xc7z030fbg676-2` chip achieving an average throughput of 750MB/s, while being clocked at 250MHz, and average of 330MB/s at 100MHz under the same workload. Both the speeds were performed in a Memory-Stream-Memory configuration using two controllers configured with AXI4 and AXI-Stream buses. The first controller reads data from memory and sends it out via an AXI-Stream interface, while the second receives the stream and writes the data received to a second buffer in memory.

Wishbone and AXI4 buses were connected to a [LiteDRAM](https://github.com/enjoy-digital/litedram) controller providing access to DDR3 memory.

Both buses used a 32-bit data bus to connect to the DDR3 controller.

In both cases the data transferred consisted of a 4MB block of randomly produced data which was subsequently verified for possible transmission errors after each transfer.

---

## Resource usage

The AXI4=\>AXI-Stream (MM2S) configuration utilized 425 slices on a `xc7z030fbg676-2` chip which was used for testing the design.

AXI-Stream=\>AXI4 (S2MM) requires 455 slices on the same chip.

Both configurations were instantiated in the same design and connected in a back-to-back configuration that allowed memory-to-memory transfers while still using configurations equipped with AXI-Stream interfaces.