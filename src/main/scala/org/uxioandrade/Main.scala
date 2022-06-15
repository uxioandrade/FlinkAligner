package org.uxioandrade

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

class Main{

}

object Main {

  val defaultParallelism =    "2"
  val defaultTumblingWindow = "30"
  val watermarkInterval =     1000L

  def printHelp(): Unit = {
    val usage =
      """
        |<-v bwa-exec> <-o output-filename> <-i fasta-filename> [-t] tumbling-window-time [-n] parallelism <-p|-s> fq-filenames
        |""".stripMargin
    println(usage)
  }

  def main(args: Array[String]): Unit = {
    if(args.length == 0) printHelp()

    val versionArg = "version"
    val parallelismArg = "parallelism"
    val fastaFileArg  = "fasta"
    val fqFileArg1 = "inputFile1"
    val fqFileArg2 = "inputFile2"
    val outputFileArg = "output"
    val tumblingWindowArg = "window"

    type ArgsMap = Map[String, String]

    def parseArgs(map: ArgsMap, list: List[String]): ArgsMap = {
      list match {
        case Nil => map
        case "-v" :: value :: tail =>
          parseArgs(map ++ Map(versionArg -> value), tail)
        case "-i" :: value :: tail =>
          parseArgs(map ++ Map(fastaFileArg -> value), tail)
        case "-t" :: value :: tail =>
          parseArgs(map ++ Map(tumblingWindowArg -> value), tail)
        case "-n" :: value :: tail =>
          parseArgs(map ++ Map(parallelismArg -> value), tail)
        case "-o" :: value :: tail =>
          parseArgs(map ++ Map(outputFileArg -> value), tail)
        case "-p" :: value1 :: value2 :: Nil =>
          map ++ Map(fqFileArg1 -> value1, fqFileArg2 -> value2)
        case "-s" :: value :: Nil =>
          map ++ Map(fqFileArg1 -> value)
        case _ =>
          printHelp()
          map
      }
    }
    val argsMap = parseArgs(Map(), args.toList)
    val version = argsMap.get(versionArg)
    val fastaFile = argsMap.get(fastaFileArg)
    val isPaired = argsMap.get(fqFileArg2).isDefined
    val inputFile1 = argsMap.get(fqFileArg1)
    val inputFile2 = argsMap.get(fqFileArg2)
    val outputFile = argsMap.get(outputFileArg)
    val windowTime = argsMap.get(tumblingWindowArg).getOrElse(defaultTumblingWindow).toInt
    val parallelism = argsMap.get(parallelismArg).getOrElse(defaultParallelism).toInt

    println(
      s"""
        | version:  ${version.get}
        | index  :  ${fastaFile.get}
        | isPaired: $isPaired
        | inputFile1: ${inputFile1.get}
        | inputFile2: ${inputFile2.getOrElse("")}
        | outputFile: ${outputFile.get}
        | parallelism: ${parallelism}
        |""".stripMargin)

    val conf = new Configuration()
    val env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    env.getConfig.setTaskCancellationTimeout(0)
    env.getConfig.setAutoWatermarkInterval(watermarkInterval)
    env.setParallelism(parallelism)
    new BWA(env).runAlignment(version.get, fastaFile.get, isPaired, inputFile1.get, inputFile2, outputFile.get, parallelism, windowTime)
    env.execute()
  }
}
