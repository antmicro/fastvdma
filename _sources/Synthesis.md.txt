# Synthesis

To generate a synthesizable verilog file either proceed with the default configuration by running:

```bash
make verilog
```
  
Or provide a valid configuration file with:

```bash
make CONFIG_FILE=<path_to_json_file> verilog
```

The generated file will be named `DMATop$(configuration).v` where `configuration` is chosen configuration of buses in the DMA. Verilog module will be named in the same manner.