Register fields
===============

Current register field layout is shown below:

Control register (0x00)
-----------------------

|Bit |Name                |Description                                                                                         |
|----|--------------------|----------------------------------------------------------------------------------------------------|
|0   |Writer start        |Write `1` to start write frontend (This bit automatically resets itself to `0` if not in loop mode) |
|1   |Reader start        |Write `1` to start read frontend (This bit automatically resets itself to `0` if not in loop mode)  |
|2   |Writer sync disable |Write `1` to disable waiting for external writer synchronization (rising edge on `writerSync`)      |
|3   |Reader sync disable |Write `1` to disable waiting for external reader synchronization (rising edge on `readerSync`)      |
|4   |Writer loop mode    |Write `1` to automatically start next write frontend transfer after finishing the current one       |
|5   |Reader loop mode    |Write `1` to automatically start next read frontend transfer after finishing the current one        |
|6-31|-                   |Unused                                                                                              |

Status register (0x04)
----------------------

|Bit |Name           |Description                                                |
|----|---------------|-----------------------------------------------------------|
|0   |Writer busy    |Reads as `1` when write frontend is busy transferring data |
|1   |Reader busy    |Reads as `1` when read frontend is busy transferring data  |
|2-31|-              |Unused                                                     |

Interrupt mask register (0x08)
------------------------------

|Bit |Name        |Description                             |
|----|------------|----------------------------------------|
|0   |Writer mask |Write `1` to enable writer interrupt    |
|1   |Reader mask |Write `1` to enable reader interrupt    |
|2-31|-           |Unused                                  |

Interrupt status register (0x0c)
--------------------------------

|Bit |Name             |Description                                                                     |
|----|-----------------|--------------------------------------------------------------------------------|
|0   |Writer interrupt |Reads as `1` if writer interrupt has occured, write `1` to clear that interrupt |
|1   |Reader interrupt |Reads as `1` if reader interrupt has occured, write `1` to clear that interrupt |
|2-31|-                |Unused                                                                          |

Reader start address (0x10)
---------------------------

|Bit |Name          |Description                                                                |
|----|--------------|---------------------------------------------------------------------------|
|0-31|Start address |Reader start address (set to `0` if reader frontend is a stream interface) |

Reader line length (0x14)
-------------------------

|Bit |Name        |Description                                              |
|----|------------|---------------------------------------------------------|
|0-31|Line length |Reader line length (as number of reader data bus widths) |

Reader line count (0x18)
------------------------

|Bit |Name       |Description       |
|----|-----------|------------------|
|0-31|Line count |Reader line count |

Reader stride between lines (0x1c)
----------------------------------

|Bit |Name   |Description                                                         |
|----|-------|--------------------------------------------------------------------|
|0-31|Stride |Gap between consecutive lines (as number of reader data bus widths) |

Writer start address (0x20)
---------------------------

|Bit |Name          |Description                                                                |
|----|--------------|---------------------------------------------------------------------------|
|0-31|Start address |Writer start address (set to `0` if writer frontend is a stream interface) |

Writer line length (0x24)
-------------------------

|Bit |Name        |Description                                              |
|----|------------|---------------------------------------------------------|
|0-31|Line length |Writer line length (as number of writer data bus widths) |

Writer line count (0x28)
------------------------

|Bit |Name       |Description       |
|----|-----------|------------------|
|0-31|Line count |Writer line count |

Writer stride between lines (0x2c)
----------------------------------

|Bit |Name   |Description                                                         |
|----|-------|--------------------------------------------------------------------|
|0-31|Stride |Gap between consecutive lines (as number of writer data bus widths) |

Version register (0x30)
-----------------------

|Bit |Name             |Description                |
|----|-----------------|---------------------------|
|0-31|Version register |Holds the FastVDMA version |

Configuration register (0x34)
-----------------------------

|Bit |Name                   |Description                          |
|----|-----------------------|-------------------------------------|
|0-31|Configuration register |Reader, writer and control bus types |
