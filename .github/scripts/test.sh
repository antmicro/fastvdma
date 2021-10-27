#!/bin/bash
export ROOT=$(pwd)
export DMACONFIG=AXIS_AXIL_AXI
mkdir $ROOT/out
make
make test
cp out.png $ROOT/out/
