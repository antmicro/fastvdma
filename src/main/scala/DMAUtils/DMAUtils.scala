package DMAUtils

import chisel3._
import play.api.libs.json._
import java.io.{FileNotFoundException, IOException}
import DMAController.DMAConfig._

abstract class DMAModule(config: DMAConfig) extends Module {
  lazy val class_name = this.getClass.getSimpleName()
  override val desiredName = s"$class_name${config.busConfig}"
}

object DMAParseInput {
  def parseconfig(filename: String): Either[DMAConfig, Unit] = {
    try {
      val resource = scala.io.Source.fromFile(filename)
      val content = Json.parse(resource.getLines().mkString)

      val config = (content \ "configuration").get.as[String]
      val addrWidth = (content \ "addrWidth").get.as[Int]
      val readDataWidth = (content \ "readDataWidth").get.as[Int]
      val writeDataWidth = (content \ "writeDataWidth").get.as[Int]
      val readMaxBurst = (content \ "readMaxBurst").get.as[Int]
      val writeMaxBurst = (content \ "writeMaxBurst").get.as[Int]
      val reader4KBarrier = (content \ "reader4KBarrier").get.as[Boolean]
      val writer4KBarrier = (content \ "writer4KBarrier").get.as[Boolean]
      val controlDataWidth = (content \ "controlDataWidth").get.as[Int]
      val controlAddrWidth = (content \ "controlAddrWidth").get.as[Int]
      val controlRegCount = (content \ "controlRegCount").get.as[Int]
      val fifoDepth = (content \ "fifoDepth").get.as[Int]

      Left(new DMAConfig(config, addrWidth, readDataWidth, writeDataWidth, readMaxBurst,
          writeMaxBurst, reader4KBarrier, writer4KBarrier, controlDataWidth, controlAddrWidth,
          controlRegCount, fifoDepth))
    } catch {
      case e: FileNotFoundException => throw e
      case e: Throwable =>
        Right(throw new Exception(f"Exception occured when parsing configuration file: ${e.getMessage()}"))
    }
  }
}

object DMAMisc {
  def printWithBg(s: String): Unit = {
    // black on magenta
    println("\u001b[30;45m" + s + "\u001b[39;49m")
  }
}
