package org.uxioandrade

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

class Main{

}

object Main {
  def printHelp(): Unit = {
    val usage =
      """
        |-o output-filename [-p|-s] fq-filenames
        |""".stripMargin
    println(usage)
  }

  def main(args: Array[String]): Unit = {
    if(args.length == 0) printHelp()

    val mockArgs = Array("-o", "srr.sam", "-i" , "./out/Homo_sapiens.GRCh37.cdna.all.fa" , "-p", "src/main/resources/ERR000589_1.filt.fastq", "src/main/resources/ERR000589_2.filt.fastq")
    val argList = mockArgs.toList
    val versionArg = "version"
    val parallelismArg = "parallelism"
    val fastaFileArg  = "fasta"
    val fqFileArg1 = "inputFile1"
    val fqFileArg2 = "inputFile2"
    val outputFileArg = "output"
    type ArgsMap = Map[String, String]
    def parseArgs(map: ArgsMap, list: List[String]): ArgsMap = {
      list match {
        case Nil => map
        case "-v" :: value :: tail =>
          parseArgs(map ++ Map(versionArg -> value), tail)
        case "-i" :: value :: tail =>
          parseArgs(map ++ Map(fastaFileArg -> value), tail)
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
    val argsMap = parseArgs(Map(), argList)
    val version = argsMap.get(versionArg)
    val fastaFile = argsMap.get(fastaFileArg)
    val isPaired = argsMap.get(fqFileArg2).isDefined
    val inputFile1 = argsMap.get(fqFileArg1)
    val inputFile2 = argsMap.get(fqFileArg2)
    val outputFile = argsMap.get(outputFileArg)
    val parallelism = argsMap.get(parallelismArg)
    println("Args:")
    println("isPaired: " + isPaired)
    println("inputFile1: " + inputFile1)
    println("inputFile2: " + inputFile2)
    println("outputFile: " + outputFile)
    val conf = new Configuration()
    val env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    env.getConfig.setTaskCancellationTimeout(0)
    env.getConfig.setAutoWatermarkInterval(1000L)
    env.setParallelism(8)
    new BWA(env).runAlignment(fastaFile.get, isPaired, inputFile1.get, inputFile2, outputFile.get)
    env.execute()
  }
}
