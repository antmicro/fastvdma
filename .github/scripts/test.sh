#!/bin/bash
export ROOT=$(pwd)
mkdir $ROOT/out
make
make test
cp out.png $ROOT/out/
