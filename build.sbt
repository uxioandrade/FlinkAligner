ThisBuild / resolvers ++= Seq(
    "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
    Resolver.mavenLocal
)

javacOptions ++= Seq("-source", "11", "-target", "11")

name := "FlinkBWA"

version := "0.1-SNAPSHOT"

organization := "org.uxioandrade"

ThisBuild / scalaVersion := "2.12.11"

val flinkVersion = "1.14.3"
val log4jVersion = "2.17.1"
val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-clients" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-runtime-web" % flinkVersion % "provided",
  )

val slf4jDependencis = Seq(
  "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion,
)

lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= flinkDependencies ++ slf4jDependencis
  )

assembly / mainClass := Some("org.uxioandrade.Job")

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
