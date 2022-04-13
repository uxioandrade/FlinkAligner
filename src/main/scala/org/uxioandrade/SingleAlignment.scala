package org.uxioandrade

import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector

import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}

case class SequenceWithTimestamp(sequence: Sequence, timestamp: Long)

class SingleAlignment(fastqFileName: String) extends KeyedProcessFunction[Int, Sequence, String]{

  private var fqFile: ValueState[String] = _
  private var currSequences: ListState[Sequence] = _
  private var currCount: ValueState[Int] = _

  override def open(parameters: Configuration): Unit = {
    currCount = getRuntimeContext.getState(new ValueStateDescriptor[Int]("file-count", classOf[Int]))
    currSequences = getRuntimeContext.getListState(new ListStateDescriptor[Sequence]("sequences", classOf[Sequence]))
    fqFile = getRuntimeContext.getState(new ValueStateDescriptor[String]("file-name", classOf[String]))
  }

  def updateFile(key: Int): Unit = {
    val countValue = currCount match {
      case null => 0
      case x => x.value()
    }
    fqFile.update("out/" + fastqFileName + key + "-" + countValue + ".fq")
    currCount.update(currCount.value() + 1)
  }

  override def processElement(
                               value: Sequence,
                               ctx: KeyedProcessFunction[Int, Sequence, String]#Context,
                               out: Collector[String]): Unit = {
    if (fqFile.value == null) {
      currCount.update(0)
      updateFile(ctx.getCurrentKey)
    }
    currSequences.add(value)
    val timeout = 100000
    val coalescedTime = ((ctx.timestamp + timeout) / 1000) * 1000
    ctx.timerService.registerEventTimeTimer(coalescedTime)
  }
  override def onTimer(
                        timestamp: Long,
                        ctx: KeyedProcessFunction[Int, Sequence, String]#OnTimerContext,
                        out: Collector[String]): Unit = {
    if (!currSequences.get.iterator().hasNext) return
    println("Writing " + fqFile.value)
    val fastqFile = new File(fqFile.value)
    val fos = new FileOutputStream((fastqFile))
    val bw = new BufferedWriter(new OutputStreamWriter(fos))

    currSequences.get().forEach(x => {
      bw.write(x.toFileString)
      bw.newLine()
    })
    bw.close()
    currSequences.clear()
    out.collect(fqFile.value)
    updateFile(ctx.getCurrentKey)
  }
}
