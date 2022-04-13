package org.uxioandrade

import org.apache.flink.api.common.functions.{RichFlatMapFunction, RichMapPartitionFunction}
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.util.Collector

import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}
import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


class SingleBWA2Alignment(fastqFileName: String) extends RichFlatMapFunction[Sequence, String] {

  private var bw: BufferedWriter = _
  private var fastqFile: File = _
  private var fos: FileOutputStream= _

  private var fqFile: String = _
  private var currSequences: ListState[Sequence] = _
  private var currCount: ValueState[Int] = _

  override def open(parameters: Configuration): Unit = {
    currCount = getRuntimeContext.getState(new ValueStateDescriptor[Int]("file-count", classOf[Int]))
//    fqFile = getRuntimeContext.getState(new ValueStateDescriptor[String]("fq-file", classOf[String]))
    currSequences = getRuntimeContext.getListState(new ListStateDescriptor[Sequence]("sequences", classOf[Sequence]))
    fqFile = ""
  }

  def updateFile(key: Int): Unit = {
    if (bw != null){
      bw.close()
    }
    val countValue = currCount match {
      case null => 0
      case x => x.value()
    }
    fqFile = "out/" + fastqFileName + key + "-" + countValue + ".fq"
    println("Writing " + fqFile)
    fastqFile = new File(fqFile)
    fos = new FileOutputStream(fastqFile)
    bw = new BufferedWriter(new OutputStreamWriter(fos))
    currCount.update(currCount.value() + 1)
  }

  def flatMap(input: Sequence, out: Collector[String]) = {
    if (fqFile.equals("")){
      currCount.update(0)
      updateFile(input.key)
    }
    currSequences.add(input)
    if (currSequences.get().toList.length == 2){
      currSequences.get().foreach(x => {
       bw.write(x.toString)
       bw.newLine()
      })
      currSequences.clear()
      val samFile = fqFile + ".sam"
      updateFile(input.key)
      val resultFuture: Int = Bwa2.run(Array("./out/bwa-mem2", "mem", "-t", "1", "-o", samFile,"./out/mini_seq.fasta", fqFile))
//      resultFuture onComplete {
//        case Success(value) =>
//          println("Bwa finished with result " + value)
//          out.collect(samFile)
//        case Failure(value) => println("Bwa failed with result " + value)
//      }
//      resultFuture onComplete {
//        case Success(value) =>
          println("Bwa finished with result " + 0)
          println("Deleting" + fqFile)
          fastqFile.delete()
          out.collect(samFile)
//        case Failure(value) =>
//          println("BWA failed with result " + value)
//      }
    }
  }

}
