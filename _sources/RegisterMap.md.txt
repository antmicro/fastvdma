# Register fields

## Register table

Current register layout is shown in the table below:

|Address | Role |
|--------|----------------------------|
|`0x00` |[Control register](control-register-0x00) |
|`0x04` |[Status register](status-register-0x04) |
|`0x08` |[Interrupt mask register](interrupt-mask-register-0x08) |
|`0x0c` |[Interrupt status register](interrupt-status-register-0x0c) |
|`0x10` |[Reader start address](reader-start-address-0x10) |
|`0x14` |[Reader line length](reader-line-length-0x14) |
|`0x18` |[Reader line count](reader-line-count-0x18) |
|`0x1c` |[Reader stride between lines](reader-stride-between-lines-0x1c) |
|`0x20` |[Writer start address](writer-start-address-0x20) |
|`0x24` |[Writer line length](writer-line-length-0x24) |
|`0x28` |[Writer line count](writer-line-count-0x28)|
|`0x2c` |[Writer stride between lines](writer-stride-between-lines-0x2c) |
|`0x30` |[Version register](version-register-0x30) |
|`0x34` |[Configuration register](configuration-register-0x34) |


## Detailed register description

### Control register (0x00)

|Bit |Name                |Description                                                                                         |
|----|--------------------|----------------------------------------------------------------------------------------------------|
|0   |Writer start        |Write `1` to start write frontend (This bit automatically resets itself to `0` if not in loop mode) |
|1   |Reader start        |Write `1` to start read frontend (This bit automatically resets itself to `0` if not in loop mode)  |
|2   |Writer sync disable |Write `1` to disable waiting for external writer synchronization (rising edge on `writerSync`)      |
|3   |Reader sync disable |Write `1` to disable waiting for external reader synchronization (rising edge on `readerSync`)      |
|4   |Writer loop mode    |Write `1` to automatically start next write frontend transfer after finishing the current one       |
|5   |Reader loop mode    |Write `1` to automatically start next read frontend transfer after finishing the current one        |
|6-31|-                   |Unused                                                                                              |

---

### Status register (0x04)

|Bit |Name           |Description                                                |
|----|---------------|-----------------------------------------------------------|
|0   |Writer busy    |Reads as `1` when write frontend is busy transferring data |
|1   |Reader busy    |Reads as `1` when read frontend is busy transferring data  |
|2-31|-              |Unused                                                     |

---

### Interrupt mask register (0x08)

|Bit |Name        |Description                             |
|----|------------|----------------------------------------|
|0   |Writer mask |Write `1` to enable writer interrupt    |
|1   |Reader mask |Write `1` to enable reader interrupt    |
|2-31|-           |Unused                                  |

---

### Interrupt status register (0x0c)

|Bit |Name             |Description                                                                     |
|----|-----------------|--------------------------------------------------------------------------------|
|0   |Writer interrupt |Reads as `1` if writer interrupt has occurred, write `1` to clear that interrupt |
|1   |Reader interrupt |Reads as `1` if reader interrupt has occurred, write `1` to clear that interrupt |
|2-31|-                |Unused                                                                          |

---

### Reader start address (0x10)

|Bit |Name          |Description                                                                |
|----|--------------|---------------------------------------------------------------------------|
|0-31|Start address |Reader start address (set to `0` if reader frontend is a stream interface) |

---

### Reader line length (0x14)

|Bit |Name        |Description                                              |
|----|------------|---------------------------------------------------------|
|0-31|Line length |Reader line length (as number of reader data bus widths) |

---

### Reader line count (0x18)

|Bit |Name       |Description       |
|----|-----------|------------------|
|0-31|Line count |Reader line count |

---

### Reader stride between lines (0x1c)

|Bit |Name   |Description                                                         |
|----|-------|--------------------------------------------------------------------|
|0-31|Stride |Gap between consecutive lines (as number of reader data bus widths) |

---

### Writer start address (0x20)

|Bit |Name          |Description                                                                |
|----|--------------|---------------------------------------------------------------------------|
|0-31|Start address |Writer start address (set to `0` if writer frontend is a stream interface) |

---

### Writer line length (0x24)

|Bit |Name        |Description                                              |
|----|------------|---------------------------------------------------------|
|0-31|Line length |Writer line length (as number of writer data bus widths) |

---

### Writer line count (0x28)

|Bit |Name       |Description       |
|----|-----------|------------------|
|0-31|Line count |Writer line count |

### Writer stride between lines (0x2c)

|Bit |Name   |Description                                                         |
|----|-------|--------------------------------------------------------------------|
|0-31|Stride |Gap between consecutive lines (as number of writer data bus widths) |

---

### Version register (0x30)

|Bit |Name             |Description                |
|----|-----------------|---------------------------|
|0-31|Version register |Holds the FastVDMA version |

---

### Configuration register (0x34)

|Bit |Name                   |Description                          |
|----|-----------------------|-------------------------------------|
|0-31|Configuration register |Reader, writer and control bus types |

---

You can also check [WorkerCSRWrapper](https://github.com/antmicro/fastvdma/blob/main/src/main/scala/DMAController/Worker/WorkerCSRWrapper.scala) for the implementation details on how the CSRs are attached to the DMA logic (`io.csr(0)` refers to `0x00`, `io.csr(1)` to `0x04` and so on).
