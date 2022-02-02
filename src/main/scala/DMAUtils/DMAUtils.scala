package DMAUtils

import chisel3._
import chisel3.util.Queue

abstract class DMAModule extends Module{
    val cfg = System.getenv("DMACONFIG")
    lazy val class_name = this.getClass.getSimpleName()
    override val desiredName = s"$class_name$cfg"
}
