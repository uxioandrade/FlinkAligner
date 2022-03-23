import sbt.internal.util.logic.Formula.True

ThisBuild / resolvers ++= Seq(
    "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
    Resolver.mavenLocal
)


javacOptions ++= Seq("-source", "11", "-target", "11")

name := "FlinkAligner"

version := "0.1-SNAPSHOT"

organization := "org.uxioandrade"

ThisBuild / scalaVersion := "2.12.15"
ThisBuild / turbo := true
classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat
val flinkVersion = "1.14.3"
val log4jVersion = "2.17.1"

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-clients" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-runtime-web" % flinkVersion % "provided",
  "org.apache.flink" % "flink-java" % flinkVersion % "provided",
  "org.apache.flink" % "flink-core" % flinkVersion % "provided",
  )

val slf4jDependencies = Seq(
  "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion,
)

val bioDependencies = Seq (
//  "it.crs4" % "rapi" % "0.1.0" from "file:///Users/uxio/tfginfo/flinkaligner/lib/jrapi.jar",
  "com.github.samtools" % "htsjdk" % "2.24.1"
)

val sbtJniDependencies = Seq(
  "com.github.sbt" % "sbt-jni-core_2.12" % "1.5.3"
)

lazy val root = (project in file(".")).aggregate(native)
  .settings(
    libraryDependencies ++= flinkDependencies ++ slf4jDependencies ++ bioDependencies
  )
  .dependsOn(native % Runtime)
lazy val native = (project in file("native"))
  .settings(nativeCompile / sourceDirectory := sourceDirectory.value)
  .enablePlugins(JniNative)

assembly / mainClass := Some("org.uxioandrade.Chunker")
// make run command include the provided dependencies
Compile / run  := Defaults.runTask(Compile / fullClasspath,
                                   Compile / run / mainClass,
                                   Compile / run / runner
                                  ).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true
// exclude Scala library from assembly
assembly / assemblyOption  := (assembly / assemblyOption).value.copy(includeScala = false)
