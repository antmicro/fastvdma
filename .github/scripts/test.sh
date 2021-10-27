#!/bin/bash
export ROOT=$(pwd)
export DMACONFIG=AXIS_AXIL_AXI
mkdir -p $ROOT/out/stream2mem_test
make
make test
cp out.png $ROOT/out/stream2mem_test

export DMACONFIG=AXI_AXIL_AXI
mkdir -p $ROOT/out/mem2mem_test
make
make test
cp out.png $ROOT/out/mem2mem_test
