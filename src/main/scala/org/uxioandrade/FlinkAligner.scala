package org.uxioandrade

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

class FlinkAligner {

  def printHelp(): Unit ={
    val usage =
      """
        |-o output-filename [-p|-s] fq-filenames
        |""".stripMargin
  }

  def main(args: Array[String]): Unit = {
    if(args.length == 0) printHelp()
    val mockArgs = Array("-o", "out.sam", "-s", "mini_seq.fq.gz")
    val argList = mockArgs.toList
    val fqFileArg1 = "inputFile1"
    val fqFileArg2 = "inputFile2"
    val outputFileArg = "output"
    type ArgsMap = Map[String, String]
    def parseArgs(map: ArgsMap, list: List[String]): ArgsMap = {
      list match {
        case Nil => map
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
    val isPaired = argsMap.get(fqFileArg2).isDefined
    val inputFile1 = argsMap.get(fqFileArg1)
    val inputFile2 = argsMap.get(fqFileArg2)
    val outputFile = argsMap.get(outputFileArg)

    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    new BWA(env).runAlignment(isPaired, inputFile1.get, inputFile2, outputFile.get)
  }

}
