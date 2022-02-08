package org.uxioandrade

import org.apache.flink.configuration.{Configuration}
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

object Chunker {

  def main(args: Array[String]): Unit = {
    val conf = new Configuration()
    val env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    val PATH = "src/main/resources/sample.fq.gz"

    val fq = env
      .readFile(new FastqInputFormat(PATH), PATH)
      .setParallelism(4)

    fq.print()
    env.execute("Fastq Read")
  }
}
