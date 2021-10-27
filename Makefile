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

test:
	convert -resize $(SIZE_HALF)x$(SIZE_HALF) $(IMG) img0.rgba
	convert -resize $(SIZE)x$(SIZE) $(IMG) img1.rgba
	$(SBT) "testOnly -t *$(TB)"
	convert -size $(SIZE)x$(SIZE) -depth 8 out.rgba out.png

testall: test
	$(SBT) "test"

clean:
	$(SBT) clean

.PHONY: verilog test testall
