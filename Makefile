SBT?=sbt
DRIVER?=DMAController.DMADriver
TB=ControllerSpec

SIZE=512
SIZE_HALF=256
IMG=bunny.png

TAG:=$(shell git describe --tags --abbrev=0)
export TAG


verilog:
	$(SBT) "runMain $(DRIVER) $(CONFIG_FILE)"

testsetup:
	convert -resize $(SIZE_HALF)x$(SIZE_HALF) $(IMG) img0.rgba
	convert -resize $(SIZE)x$(SIZE) $(IMG) img1.rgba

testM2M: testsetup
	$(SBT) 'Test / testOnly *$(TB) -- -z "MM2MM transfer"'
	convert -size $(SIZE)x$(SIZE) -depth 8 outAXI_AXIL_AXI.rgba outM2M.png

testS2M: testsetup
	$(SBT) 'Test / testOnly *$(TB) -- -z "S2MM transfer"'
	convert -size $(SIZE)x$(SIZE) -depth 8 outAXIS_AXIL_AXI.rgba outS2M.png

testM2M64: testsetup
	$(SBT) 'Test / testOnly *$(TB) -- -z "MM2MM 64-bit start address transfer"'
	convert -size $(SIZE)x$(SIZE) -depth 8 outAXI_AXIL_AXI_64.rgba outM2M_64.png

testS2M64: testsetup
	$(SBT) 'Test / testOnly *$(TB) -- -z "S2MM 64-bit start address transfer"'
	convert -size $(SIZE)x$(SIZE) -depth 8 outAXIS_AXIL_AXI_64.rgba outS2M_64.png

test: testS2M testM2M testS2M64 testM2M64

testall: test
	$(SBT) "test"

clean:
	$(SBT) clean

.PHONY: verilog testsetup testM2M testS2M test testall clean

%:
	sphinx-build -M $@ docs build $(SPHINXOPTS) $(0)

