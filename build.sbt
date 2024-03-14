// See README.md for license details.

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // If we're building with Scala > 2.11, enable the compile option
    //  switch to support our anonymous Bundle definitions:
    //  https://github.com/scala/bug/issues/10047
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 => Seq()
      case _ => Seq("-Xsource:2.13")
    }
  }
}

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

ThisBuild / organization := "Antmicro"
ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "2.13.12"
name := "FastVDMA"

val chiselVersion = "6.2.0"
crossScalaVersions := Seq("2.11.12", "2.13.12")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

// Library name, Organization, Version
val defaultVersions = Map(
  "chisel" -> "org.chipsalliance" -> chiselVersion,
  "chiseltest" -> "edu.berkeley.cs" -> "6.0-SNAPSHOT",
  "chisel-iotesters" -> "edu.berkeley.cs" -> "2.5.5+",
  "play-json" -> "com.typesafe.play" -> "2.8.+",
  "scalatest" -> "org.scalatest" -> "3.2.16"
)

// Provide a managed dependency on X if -DXVersion="" is supplied on the command line.
libraryDependencies ++= defaultVersions.map {
  case ((dep: String, org: String), v: String) => {
    org %% dep % sys.props.getOrElse(dep + "Version", v)
  }
}.toSeq

// Compiler plugin (necessary from chisel 5.0)
addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full)

scalacOptions ++= scalacOptionsVersion(scalaVersion.value) ++ Seq(
  "-language:reflectiveCalls",
  "-deprecation",
  "-feature",
  "-Xcheckinit",
  "-Ymacro-annotations"
)
javacOptions ++= javacOptionsVersion(scalaVersion.value)
