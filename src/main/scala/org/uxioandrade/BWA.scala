package org.uxioandrade

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.functions.source.FileProcessingMode
import org.apache.flink.streaming.api.datastream.AsyncDataStream
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

import java.time.{Duration, Instant}
import java.util.concurrent.TimeUnit

class BWA(env: StreamExecutionEnvironment) {

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

  def getKeyedPairedReads(path1: String, path2: String) = {
    val fq1DS = env
      .readFile(new FastqInputFormat(path1), path1, FileProcessingMode.PROCESS_ONCE, 500)
    val fq2DS = env
      .readFile(new FastqInputFormat(path2), path2, FileProcessingMode.PROCESS_ONCE, 500)
    val fqDS: DataStream[PairedSequence] = fq1DS
      .join(fq2DS)
      .where(el => el.identifier)
      .equalTo(el => el.identifier)
      .window(TumblingEventTimeWindows.of(Time.seconds(20)))
      .apply(new PairedReadsJoinFunction)
    fqDS
      .keyBy(new PairedSequenceKeySelector)
      .process(new PairedBWA2Alignment("mini_seq.fastq"))
  }


  def getKeyedSingleReads(path: String) = {
    val samDs = env
     .readFile(new FastqInputFormat(path), path, FileProcessingMode.PROCESS_ONCE,500)
     .setParallelism(8)
     .assignTimestampsAndWatermarks(ws)
      .keyBy(new SequenceKeySelector)
      .process(new SingleAlignment("sample.fastq"))
//     samDs.print()
//    samDs.print()
    //     .setParallelism(4)
//     .rebalance()
//    val f =
//      samDs
//        .flatMap(new SingleBWA2Alignment("mini_seq.fastq"))
//    f.print()
//    samDs.print()
    val finalDs = AsyncDataStream.orderedWait(samDs, new AsyncBWAFunc, 200, TimeUnit.SECONDS)
//    finalDs.print()
//    finalDs.forceNonParallel()
//    finalDs.print()
//    val jobClient = env.execute("Single alignment")
//    println(jobClient)
    val samFileName = "out/sample2.sam"
    finalDs.flatMap(new SAMCombiner(samFileName))
//    Await.ready(f, CDuration.apply(30000,"s"))
//    f.onComplete(x => x.get.print())
  }

}
