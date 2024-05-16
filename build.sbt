// See README.md for license details.

def javacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // Scala 2.12 requires Java 8. We continue to generate
    //  Java 7 compatible code for Scala 2.11
    //  for compatibility with old clients.
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 =>
        Seq("-source", "1.7", "-target", "1.7")
      case _ =>
        Seq("-source", "1.8", "-target", "1.8")
    }
  }
}

name := "chisel-dma"

version := "6.0.0"

scalaVersion := "2.13.12"

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)
resolvers -= DefaultMavenRepository
resolvers += "Maven Repo" at "https://mvnrepository.com/artifacts"

// Chisel 6.0.0
addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % "6.0.0" cross CrossVersion.full)

// Provide a managed dependency on X if -DXVersion="" is supplied on the command line.
val defaultVersions = Map(
  "chisel" -> "6.0.0",
  "chiseltest" -> "6.0-SNAPSHOT"
)

val defaultOrgs = Map(
  "chisel" -> "org.chipsalliance",
  "chiseltest" -> "edu.berkeley.cs"
)

libraryDependencies ++= Seq("chisel","chiseltest").map {
  dep: String => defaultOrgs(dep) %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep)) }

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.5"

javacOptions ++= javacOptionsVersion(scalaVersion.value)