package org.uxioandrade

import org.apache.flink.api.common.{JobExecutionResult, RuntimeExecutionMode}
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.execution.{JobClient, JobListener}
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}
/*
object BWARunner extends App{

  val PATH = "src/main/resources/sample.fastq.gz"
  val conf = new Configuration()
  val env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
  env.setParallelism(8)
  env.registerJobListener(new JobListener {
    override def onJobSubmitted(jobClient: JobClient, throwable: Throwable): Unit = throwable match {
      case null => println("Submit success")
      case _ => println("Job Submission failed")
    }

    override def onJobExecuted(jobExecutionResult: JobExecutionResult, throwable: Throwable): Unit =  throwable match {
      case null => println("Executed success")
      case _ => println(throwable)
    }
  })
//  val ds = env.fromElements("out/sam1.sam", "out/sam2.sam")
//  val samFileName = "out/mini_seq.sam"
//  ds.flatMap(new SAMCombiner(samFileName)).print()
//  env.execute()
//    new BWA(env).runSingleAlignment(PATH,)
  env.execute("Single Align")
//  Thread.sleep(10000)
}
*/