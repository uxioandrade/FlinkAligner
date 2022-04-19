package org.uxioandrade

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.functions.source.FileProcessingMode
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.mutable.Set
import scala.collection.JavaConverters.{asScalaIteratorConverter, seqAsJavaListConverter}

class FASTQInputFormatTest extends AnyFlatSpec{
  val env = StreamExecutionEnvironment.createLocalEnvironment

  val sequences = Seq(
    Sequence(
      0,
      "@read_00/1",
      "CCCACTGACCCACACAGAAAAACTAATTGTGAGAACCAATATTATACTAAATTCATTTGA",
      "EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE"
    ),
      Sequence(
      1,
      "@read_00/2",
      "TTTACTGACCCACACAGAAAAACTAATTGTGAGAACCAATATTATACTAAATTCATTTGA",
      "EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE"
    ),
      Sequence(
        0,
        "@read_00/3",
        "AAAACTGACCCACACAGAAAAACTAATTGTGAGAACCAATATTATACTAAATTCATTTGA",
        "EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE"
      )
  )

  val fastqFile = getClass
    .getClassLoader
    .getResource("sequence.fq.gz")
    .getPath

  val malformattedFile = getClass
    .getClassLoader
    .getResource("sequence.fq.gz")
    .getPath

  val emptyFile = getClass
    .getClassLoader
    .getResource("empty.fq.gz")
    .getPath

  behavior of "Fastq Input Format"

  it should "read fastq string into a sequence stream" in {
    val fastqDs = env.readFile(new FastqInputFormat(fastqFile), fastqFile, FileProcessingMode.PROCESS_ONCE,500)
    val fastqList = fastqDs.executeAndCollect()
    assert(fastqList.next() == sequences.head)
  }

  it should "assign different keys to each sequence" in {
    val keyMaxValue = 2
    val fastqDs = env.readFile(new FastqInputFormat(fastqFile, keyMaxValue), fastqFile, FileProcessingMode.PROCESS_ONCE,500)
    val fastqList = fastqDs.executeAndCollect()
    var keySet = Set[Int]()
    fastqList.asScala.foreach(x => keySet add x.key)
    assert(keySet.size == keyMaxValue)
  }

  it should "ignore the malformatted sequences in a file" in {
    val keyMaxValue = 2
    val fastqDs = env.readFile(new FastqInputFormat(malformattedFile, keyMaxValue), malformattedFile, FileProcessingMode.PROCESS_ONCE,500)
    val fastqList: java.util.List[Sequence] = fastqDs.executeAndCollect(sequences.size)
    assert(fastqList == sequences.asJava)
  }

  it should "read an empty fq file into an empty datastream" in {
    val keyMaxValue = 2
    val fastqDs = env.readFile(new FastqInputFormat(emptyFile, keyMaxValue), emptyFile, FileProcessingMode.PROCESS_ONCE, 500)
    val fastqList = fastqDs.executeAndCollect(2)
    assert(fastqList.isEmpty)
  }
}
