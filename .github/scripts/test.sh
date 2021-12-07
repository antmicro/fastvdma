#!/bin/bash
export ROOT=$(pwd)
mkdir -p $ROOT/out/stream2mem_test
mkdir -p $ROOT/out/mem2mem_test

# Components test
export DMACONFIG=AXIS_AXIL_AXI
make
sbt "test:testOnly *ComponentSpec"

# Stream to memory test
make test
cp out.png $ROOT/out/stream2mem_test

# Memory to memory test
export DMACONFIG=AXI_AXIL_AXI
make
make test
cp out.png $ROOT/out/mem2mem_test
