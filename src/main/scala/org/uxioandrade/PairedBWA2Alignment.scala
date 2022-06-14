package org.uxioandrade

import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector

import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}

class PairedBWA2Alignment(fastqFileName: String) extends KeyedProcessFunction[Int, PairedSequence, (String, String)]{

  private var fqFile: ValueState[String] = _
  private var currSequences: ListState[PairedSequence] = _
  private var currCount: ValueState[Int] = _
  private var prevTimestamp: ValueState[Long] = _

  override def open(parameters: Configuration): Unit = {
    currCount = getRuntimeContext.getState(new ValueStateDescriptor[Int]("file-count", classOf[Int]))
    currSequences = getRuntimeContext.getListState(new ListStateDescriptor[PairedSequence]("sequences", classOf[PairedSequence]))
    fqFile = getRuntimeContext.getState(new ValueStateDescriptor[String]("file-name", classOf[String]))
    prevTimestamp = getRuntimeContext.getState(new ValueStateDescriptor[Long]("prev-timestamp", classOf[Long]))
  }

  def updateFile(key: Int): Unit = {
    val countValue = currCount match {
      case null => 0
      case x => x.value()
    }
    fqFile.update(fastqFileName + key + "-" + countValue)
    currCount.update(currCount.value() + 1)
  }

  override def processElement(
                               value: PairedSequence,
                               ctx: KeyedProcessFunction[Int, PairedSequence, (String, String)]#Context,
                               out: Collector[(String, String)]): Unit = {
    if (fqFile.value == null) {
      currCount.update(0)
      updateFile(ctx.getCurrentKey)
    }
    val prevTimestampValue = prevTimestamp match {
      case null => 0
      case x => x.value()
    }
    currSequences.add(value)
    val timeout = 10000L
    if(prevTimestampValue + timeout > ctx.timestamp()) return
    prevTimestamp.update(ctx.timestamp())
    val coalescedTime = ((ctx.timestamp + timeout) / 1000) * 1000
    ctx.timerService.registerProcessingTimeTimer(coalescedTime)
  }

  private def getFqFileBufferedWriter(fqFileName: String): BufferedWriter={
    val fastqFile = new File(fqFileName)
    val fos = new FileOutputStream((fastqFile))
    new BufferedWriter(new OutputStreamWriter(fos))
  }

  override def onTimer(
                        timestamp: Long,
                        ctx: KeyedProcessFunction[Int, PairedSequence, (String, String)]#OnTimerContext,
                        out: Collector[(String, String)]): Unit = {
    println("No timer")
    if (!currSequences.get.iterator().hasNext) return
    println("Timer go")

    val fq1FileName = fqFile.value + "_1.fq"
    val fq2FileName = fqFile.value + "_2.fq"
    val bw1 = getFqFileBufferedWriter(fq1FileName)
    val bw2 = getFqFileBufferedWriter(fq2FileName)

    currSequences.get().forEach(x => {
      bw1.write(x.seq1.toFileString)
      bw1.newLine()
      bw2.write(x.seq2.toFileString)
      bw2.newLine()
    })
    bw1.close()
    bw2.close()
    currSequences.clear()
    out.collect((fq1FileName, fq2FileName))
    updateFile(ctx.getCurrentKey)
  }
}