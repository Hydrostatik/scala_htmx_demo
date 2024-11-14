import org.typelevel.sbt.tpolecat.*

ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "3.5.2"

// This disables fatal-warnings for local development. To enable it in CI set the `SBT_TPOLECAT_CI` environment variable in your pipeline.
// See https://github.com/typelevel/sbt-tpolecat/?tab=readme-ov-file#modes
ThisBuild / tpolecatDefaultOptionsMode := VerboseMode

lazy val root = (project in file(".")).settings(
  name := "scala_htmx_demo",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "2.12.0",
    // "core" module - IO, IOApp, schedulers
    // This pulls in the kernel and std modules automatically.
    "org.typelevel" %% "cats-effect" % "3.5.3",
    // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
    "org.typelevel" %% "cats-effect-kernel" % "3.5.3",
    // standard "effect" library (Queues, Console, Random etc.)
    "org.typelevel" %% "cats-effect-std" % "3.5.3",
    "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test,
    // Functional JDBC library for Scala
    "org.tpolecat" %% "doobie-core" % "1.0.0-RC5",
    "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC5",
    "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC5",
    "org.postgresql" % "postgresql" % "42.5.3"
  )
)
