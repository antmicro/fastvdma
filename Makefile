SBT?=sbt
DRIVER?=DMAController.DMADriver
TB=ControllerSpec

SIZE=512
SIZE_HALF=256
IMG=bunny.png

TAG:=$(shell git describe --tags --abbrev=0)
export TAG

verilog:
	$(SBT) "runMain $(DRIVER)"

testsetup:
	convert -resize $(SIZE_HALF)x$(SIZE_HALF) $(IMG) img0.rgba
	convert -resize $(SIZE)x$(SIZE) $(IMG) img1.rgba

testM2M: export DMACONFIG = AXI_AXIL_AXI
testM2M: testsetup 
	$(SBT) "Test / testOnly -t *$(TB)"
	convert -size $(SIZE)x$(SIZE) -depth 8 out.rgba outM2M.png

testS2M: export DMACONFIG = AXIS_AXIL_AXI
testS2M: testsetup
	$(SBT) "Test / testOnly -t *$(TB)"
	convert -size $(SIZE)x$(SIZE) -depth 8 out.rgba outS2M.png

test: testS2M testM2M

testall: test
	$(SBT) "test"

clean:
	$(SBT) clean

.PHONY: verilog test testall
