# Writing custom tests  

If you would like to reuse provided tests to test your custom model you will need to write a test file similar to [DMAFullMem](https://github.com/antmicro/fastvdma/blob/main/src/test/scala/DMAController/DMAFullMem.scala).

What you need to alter is:

- Cast buses in the `io` field accordingly to chosen configuration.
  Example for `AXIS_AXIL_AXI` bus configuration:

```scala
val io = dut.io.asInstanceOf[Bundle{
			val control: AXI4Lite
			val read: AXIStream
			val write: AXI4
			val irq: InterruptBundle
			val sync: SyncBundle}]
```

- You will also need to remember to provide correct BFMs for the test:

```scala 
val control = new AxiLiteMasterBfm(io.control, peek, poke, println)
val reader = new AxiStreamMasterBfm(io.read, width, peek, poke, println)
val writer = new Axi4SlaveBfm(io.write, width * height, peek, poke, println)
```

- Lastly, add an entry in the [ControllerSpec](https://github.com/antmicro/fastvdma/blob/main/src/test/scala/DMAController/ControllerSpec.scala) (or write your Tester):

```scala
val myConfiguration = new DMAConfig(...)
it should "perform image transfer with my custom configuration" in {
	test(new DMATop(myConfiguration)).runPeekPoke(dut =>
		new ImageTransfer(dut, new <CustomTestClassName>(dut), myConfiguration)
	)
}
```

After successful test, the image `out$(configuration).rgba` will be produced (where `configuration` is the DMA bus configuration).