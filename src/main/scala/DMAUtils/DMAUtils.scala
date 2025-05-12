package DMAUtils

import chisel3._
import chisel3.util._
import play.api.libs.json._
import java.io.{FileNotFoundException, IOException}
import DMAController.DMAConfig._

abstract class DMAModule(implicit dmaConfig: DMAConfig) extends Module {
  lazy val class_name = this.getClass.getSimpleName()
  override val desiredName = s"$class_name${dmaConfig.busConfig}"
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

object DMALogger {
  private sealed abstract class Color
  private object Red extends Color
  private object Orange extends Color
  private object Green extends Color
  private object Magenta extends Color
  private val escape = "\u001b[0m"

  private val colorToAnsi = Map(
    Red -> "\u001b[31m",
    Orange -> "\u001b[38;5;208m",
    Green -> "\u001b[32m",
    Magenta -> "\u001b[35m"
  )

  private val colorToAnsiBackground = Map(
    Red -> "\u001b[41;1m",
    Orange -> "\u001b[48;5;208m",
    Green -> "\u001b[42;1m",
    Magenta -> "\u001b[45;1m"
  )

  private abstract class LogLevel
  private object Error
  private object Warn
  private object Info
  private object Debug

  private val levelEnv = System.getenv("LOG_LEVEL")
  private val logLevel = levelEnv match {
    case "Error" => Error
    case "Warn"  => Warn
    case "Debug" => Debug
    case _       => Info
  }

  def isDebugEnabled(): Boolean = logLevel == Debug
  def isInfoEnabled(): Boolean = isDebugEnabled() | logLevel == Info
  def isWarnEnabled(): Boolean = isInfoEnabled() | logLevel == Warn
  def isErrorEnabled(): Boolean = isWarnEnabled() | logLevel == Error

  def debug(msg: String): Unit =
    if (isDebugEnabled()) println(s"[DEBUG] ${msg}")
  def info(msg: String): Unit =
    if (isInfoEnabled())
      println(s"${colorToAnsi(Green)}[INFO] ${msg}${escape}")
  def warn(msg: String): Unit =
    if (isWarnEnabled())
      println(s"${colorToAnsi(Orange)}[WARN] ${msg}${escape}")
  def error(msg: String): Unit =
    if (isErrorEnabled()) println(s"${colorToAnsi(Red)}[ERROR] ${msg}${escape}")
}

class DMAQueue[T <: Data](gen: T)(implicit dmaConfig: DMAConfig)
    extends Queue(gen, dmaConfig.fifoDepth) {
      override val desiredName = s"DMAQueue${dmaConfig.busConfig}"
}

object DMAQueue {
    def apply[T <: Data](
        enq: ReadyValidIO[T],
        flush: Option[Bool] = None
    )(implicit dmaConfig: DMAConfig): DecoupledIO[T] = {
      if (dmaConfig.fifoDepth == 0) {
        val deq = Wire(new DecoupledIO(chiselTypeOf(enq.bits)))
        deq.valid := enq.valid
        deq.bits := enq.bits
        enq.ready := deq.ready
        deq
      } else {
        val q = Module(new DMAQueue(chiselTypeOf(enq.bits)))
        q.io.flush.zip(flush).foreach(f => f._1 := f._2)
        q.io.enq.valid := enq.valid
        q.io.enq.bits := enq.bits
        enq.ready := q.io.enq.ready
        q.io.deq
      }
    }
}
