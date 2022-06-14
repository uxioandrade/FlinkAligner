package org.uxioandrade

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.functions.source.FileProcessingMode
import org.apache.flink.streaming.api.datastream.AsyncDataStream
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction
import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time

import java.time.{Duration, Instant}
import java.util.concurrent.TimeUnit

class BWA(env: StreamExecutionEnvironment) {

  val pairedWs = WatermarkStrategy
    .forBoundedOutOfOrderness[PairedSequence](Duration.ofSeconds(60))
    .withIdleness(Duration.ofSeconds(60))
    .withTimestampAssigner(new SerializableTimestampAssigner[PairedSequence] {
      override def extractTimestamp(element: PairedSequence, recordTimestamp: Long): Long =
        recordTimestamp match {
          case x if x < 0 => Instant.now.toEpochMilli
          case y => y
        }
    })

  val ws = WatermarkStrategy
    .forBoundedOutOfOrderness[Sequence](Duration.ofSeconds(20))
    .withIdleness(Duration.ofSeconds(20))
    .withTimestampAssigner(new SerializableTimestampAssigner[Sequence] {
      override def extractTimestamp(element: Sequence, recordTimestamp: Long): Long =
        recordTimestamp match {
          case x if x < 0 => Instant.now.toEpochMilli
          case y => y
        }
    })

  def runPairedAlignment(fastaFile: String, path1: String, path2: String, outputFilename: String) = {
    val fq1DS = env.addSource(new FastqSourceFunction(path1, 4))
      .setParallelism(4)
      .assignTimestampsAndWatermarks(ws)
    val fq2DS = env.addSource(new FastqSourceFunction(path2, 4))
      .setParallelism(4)
      .assignTimestampsAndWatermarks(ws)
    val fqDS = fq1DS
      .join(fq2DS)
      .where(el1 => el1.identifier.substring(0, el1.identifier.length-2))
      .equalTo(el2 => el2.identifier.substring(0, el2.identifier.length-2))
      .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
      .apply(new PairedReadsJoinFunction)
      .assignTimestampsAndWatermarks(pairedWs)
      .keyBy(new PairedSequenceKeySelector)
      .process(new PairedBWA2Alignment(path1))

    val finalDs = AsyncDataStream.orderedWait(fqDS, new AsyncPairedBWAFunc, 200, TimeUnit.SECONDS)
    val output = finalDs.flatMap(new SAMCombiner(outputFilename))
  }


  def runSingleAlignment(fastaFile: String, path: String, outputFilename: String) = {
    val samDs = env.addSource(new FastqSourceFunction(path, 8))
      .setParallelism(8)
      .assignTimestampsAndWatermarks(ws)
      .keyBy(new SequenceKeySelector)
      .process(new SingleAlignment(path))

//    val samDs = env
//     .readFile(new FastqInputFormat(path, 8), path, FileProcessingMode.PROCESS_ONCE,500).setParallelism(8)
//     .assignTimestampsAndWatermarks(ws)
//     .keyBy(new SequenceKeySelector)
//     .process(new SingleAlignment(path))
    val finalDs = AsyncDataStream.orderedWait(samDs, new AsyncBWAFunc(fastaFile), 200, TimeUnit.SECONDS)
    val output = finalDs.flatMap(new SAMCombiner(outputFilename))
//    output.print()
    env.execute()
  }

  def runAlignment(fastaFile: String, isPaired: Boolean, inputFile1: String, inputFile2: Option[String], outputFile: String) = {
    if(isPaired){
      runPairedAlignment(fastaFile, inputFile1, inputFile2.get, outputFile)
    } else {
      runSingleAlignment(fastaFile, inputFile1, outputFile)
    }
  }

}
