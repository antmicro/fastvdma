import mill._
import mill.scalalib._
import mill.scalalib.scalafmt._

object ivys {
  val scalaVersion = "2.13.12"

  val ivyVersions = Map(
    "org.chipsalliance::chisel" -> "6.0.0",
    "org.chipsalliance:::chisel-plugin" -> "$chisel",
    "edu.berkeley.cs::chiseltest" -> "6.0.0",
    "edu.berkeley.cs::firrtl2" -> "$chiseltest",
    "org.scalatest::scalatest" -> "3.2.17",
    "org.scalacheck::scalacheck" -> "1.17.0",
    "org.scalatestplus::scalacheck-1-17" -> "3.2.17.0",
    "com.lihaoyi::mainargs" -> "0.5.4",
    "com.outr::scribe" -> "3.13.0",
    "com.lihaoyi::pprint" -> "0.8.1",
    "com.lihaoyi::os-lib" -> "0.9.2",
    "org.json4s::json4s-jackson" -> "4.0.7",
    "org.scala-lang.modules::scala-parallel-collections" -> "1.0.4",
    "com.lihaoyi::utest" -> "0.8.2",
    "org.scalanlp::breeze" -> "2.1.0",
    "com.chuusai::shapeless" -> "2.3.10",
    "com.github.jnr:jnr-ffi" -> "2.2.15",
    "org.scala-lang:scala-reflect" -> scalaVersion,
    "com.typesafe.play::play-json" -> "2.10.4"
  )

  val nameMap = Map.from(ivyVersions.map { case (k, v) =>
    val kSplit = k.split(':')
    kSplit.last -> (k, v)
  })

  def lookup(name: String): (String, String) =
    nameMap.getOrElse(name, (name, ivyVersions(name)))

  @annotation.tailrec
  def getVersion(version: String): String = {
    if (version.startsWith("$")) {
      val v = lookup(version.stripPrefix("$"))._2
      getVersion(v)
    } else version
  }

  def dep(name: String): Dep = {
    val (fqn, v) = lookup(name)
    ivy"$fqn:${getVersion(v)}"
  }
}

trait CommonModule extends mill.Module with CoursierModule {

  override def repositoriesTask = T.task {
    import coursier.maven.MavenRepository

    super.repositoriesTask() ++ Seq( //
      MavenRepository("https://oss.sonatype.org/content/repositories/releases"),
      MavenRepository(
        "https://oss.sonatype.org/content/repositories/snapshots"
      ),
      MavenRepository(
        "https://s01.oss.sonatype.org/content/repositories/releases"
      ),
      MavenRepository(
        "https://s01.oss.sonatype.org/content/repositories/snapshots"
      ),
      MavenRepository("https://jitpack.io"),
      MavenRepository(s"file://${os.home}/.m2/repository")
    )
  }

  def dep(name: String) = ivys.dep(name)
}

trait CommonScalaModule
    extends CommonModule
    with ScalaModule
    with ScalafmtModule {
  override def scalaVersion = ivys.scalaVersion

  def noWarnUnused = Seq(
    "-Wconf:cat=unused:silent"
  )

  def noWarn = Seq(
//    "-Wconf::s",
  )

  override def scalacOptions = Seq(
    // checks
    "-deprecation",
    "-feature",
    "-Xcheckinit",
    // warnings
    "-Wunused",
    "-Xlint:adapted-args",
    "-Wconf:cat=unused&msg=parameter .* in .* never used:silent",
    "-Wconf:src=dependencies/.*:silent"
  )
}

// For modules not support mill yet, need to have a ScalaModule depend on our own repositories.
trait ChiselModule extends CommonScalaModule {
  override def ivyDeps = super.ivyDeps() ++ Agg(
    dep("chisel"),
    dep("scala-reflect")
  )

  override def scalacPluginIvyDeps = Agg(
    dep("chisel-plugin")
  )

  override def scalacOptions = super.scalacOptions() ++ Seq(
    // chisel:
    "-Ymacro-annotations",
    "-language:reflectiveCalls",
    // ignore warning for arguments starting with an underscore
    "-Wconf:cat=unused&msg=parameter _.* in .* is never used:s",
    "-Wconf:cat=deprecation&msg=Importing from firrtl is deprecated:s",
    "-Wconf:cat=deprecation&msg=will not be supported as part of the migration to the MLIR-based FIRRTL Compiler:s"
  )
}

trait InnerChiselTestModule
    extends CommonScalaModule
    with TestModule.ScalaTest {
  override def ivyDeps = super.ivyDeps() ++ Agg(
    dep("chiseltest"),//.excludeName("chisel", "chisel-plugin", "scalatest"),
    dep("scalatest"),
    dep("scalacheck")
  )
}

trait MacrosModule extends ChiselModule {
  override def ivyDeps = super.ivyDeps() ++ Agg(
    dep("scala-reflect")
  )

  override def scalacOptions = super.scalacOptions() ++ Seq(
    "-language:experimental.macros"
  )
}

///=============================================================================================///

object vdma extends SbtModule with ChiselModule {
  override def millSourcePath = super.millSourcePath / os.up
  object test extends InnerChiselTestModule with SbtModuleTests {
    //
  }
  override def ivyDeps = super.ivyDeps() ++ Agg(
    dep("play-json")
  )
}
///=============================================================================================///
