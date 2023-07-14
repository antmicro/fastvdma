# Configuration

## Configuration example

The FastVDMA is configurable via the JSON configuration file. Here's an example of FastVDMA configured to read from AXI-Stream interface and write to memory with AXI4 interface:

```json
{
    "configuration": "AXIS_AXIL_AXI",
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

:::{info}
All of the above parameters have to be specified to generate FastVDMA core.
:::

To build a FastVDMA core with custom configuration pass the path to your config file via the `CONFIG_FILE` variable:

```bash
make CONFIG_FILE=<path_to_json_file> verilog
```

## Configuration parameters

### configuration

This field is used to specify the FastVDMA bus configuration. It's a string of format:

```
BUS-IN_BUS-CSR_BUS-OUT
```

Where the `BUS-IN` and `BUS-OUT` are the data transferring buses and `BUS-CSR` is the CSR handling bus type.

List of all supported DMA bus configurations is available in the [DMAConfig](https://github.com/antmicro/fastvdma/blob/main/src/main/scala/DMAController/DMAConfig.scala) file.

---

### addrWidth

This field specifies address width of the data transferring buses.

---

### readDataWidth

This field specifies the data width of the `BUS-IN` bus.

---

### writeDataWidth

This field specifies the data width of the `BUS-OUT` bus.

---

### readMaxBurst

This field specifies the maximal burst for the `BUS-IN` bus. That is, the maximum number of bytes within the single transaction.

:::{admonition}
The `0`-value is reserved for the stream interfaces.
:::

---

### writeMaxBurst

This field specifies the maximal burst for the `BUS-OUT` bus. That is, the maximum number of bytes within the single transaction.

:::{admonition}
The `0`-value is reserved for the stream interfaces.
:::

---

### reader4KBarrier

This field takes the `true`/`false` value whether the `BUS-IN` interface may cross 4KB (4096 bytes) in a single transfer.

---

### writer4KBarrier

This field takes the `true`/`false` value whether the `BUS-OUT` interface may cross 4KB (4096 bytes) in a single transfer.

---

### controlDataWidth

This field specifies the data width of the `BUS-CSR` bus.

---

### controlAddrWidth

This field specifies the address width of the `BUS-CSR` bus.

:::{admonition}
The FastVDMA was written with enhancements in mind.
It is not recommended to use address widths other than **32**.
:::

---

### controlRegCount

This field specifies the number of the registers the `BUS-CSR` handles. It needs to be at least **16**.

---

### fifoDepth

This parameter specifies the depth of the queue initialized and connected between `BUS-IN` and `BUS-OUT` interfaces.
