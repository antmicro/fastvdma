import mill._
import mill.scalalib._
import mill.scalalib.scalafmt._

object ivys {
  val scalaVersion = "2.13.12"

  val ivyVersions = Map(
    "org.chipsalliance::chisel" -> "6.0.0",
    "org.chipsalliance:::chisel-plugin" -> "$chisel",
    "edu.berkeley.cs::chiseltest" -> "6.0-LOCAL-SNAPSHOT",
    "com.lihaoyi::mainargs" -> "0.5.4+",
    "org.scala-lang:scala-reflect" -> scalaVersion,
    "com.typesafe.play::play-json" -> "2.10.4+"
  )

  lazy val nameMap = Map.from(ivyVersions.map { case (k, v) =>
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

  override def scalacOptions = Seq(
    // checks
    "-deprecation",
    "-feature",
    "-Xcheckinit",
    // warnings
    // "-Wunused",
    "-Xlint:adapted-args",
    "-Wconf:cat=unused&msg=parameter .* in .* never used:silent",
    "-Wconf:src=dependencies/.*:silent"
  )
}

trait ChiselModule extends CommonScalaModule {
  override def ivyDeps = super.ivyDeps() ++ Agg(
    dep("chisel")
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
    dep("chiseltest")
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
