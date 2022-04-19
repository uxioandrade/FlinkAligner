package org.uxioandrade

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.functions.async.ResultFuture
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec

import java.io.File
import java.nio.file.{Files, Paths}
import scala.collection.JavaConverters._

class BWATest extends AnyFlatSpec with MockFactory with BeforeAndAfter{
  val env = StreamExecutionEnvironment.getExecutionEnvironment

  val inputString =
    """
      |@read_00/1
      |CCCACTGACCCACACAGAAAAACTAATTGTGAGAACCAATATTATACTAAATTCATTTGA
      |+
      |EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
      |@read_00/2
      |TTTACTGACCCACACAGAAAAACTAATTGTGAGAACCAATATTATACTAAATTCATTTGA
      |+
      |EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
      |@read_00/3
      |AAAACTGACCCACACAGAAAAACTAATTGTGAGAACCAATATTATACTAAATTCATTTGA
      |+
      |EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
      |
      |""".stripMargin

  val inputFile = getClass
    .getClassLoader
    .getResource("sequence.fq")
    .getPath

  val outputSamFile = getClass
    .getClassLoader
    .getResource("combined.sam")
    .getPath

  val sequenceSamFile = getClass
    .getClassLoader
    .getResource("sequence.sam")
    .getPath

  val emptyFq = getClass
    .getClassLoader
    .getResource("empty.fq")
    .getPath

  def samFile = outputSamFile + ".tmp"

  after {
    val inputFqFile = new File(inputFile)
    inputFqFile.delete()
    if(!inputFqFile.isFile) {
      Files.createFile(Paths.get(inputFqFile.getPath))
      Files.write(Paths.get(inputFqFile.getPath),inputString.getBytes())
    }
    val samInputFile = new File(inputFile + ".sam")
    samInputFile.delete()
    val samOutputFile = new File(samFile)
    samOutputFile.delete()
    val emptySamFile = new File(emptyFq)
    emptySamFile.delete()
    val emptyFqFile = new File(emptyFq)
    if(!emptyFqFile.isFile) {
      Files.createFile(Paths.get(emptyFqFile.getPath))
    }
  }

  behavior of "RunBwaProcess"
  it should "Run the BWA process and produce a SAM file" in {
    val inputSamFile = inputFile + ".sam"
    new AsyncBWAFunc().runBWAProcess(inputSamFile, inputFile)
    Common.compareSams(inputSamFile, sequenceSamFile)
  }

  it should "Run the BWA process when calling asyncInvoke" in {
    val inputSamFile = inputFile + ".sam"
    val mockFuture = mock[ResultFuture[String]]
    (mockFuture.complete _).expects(List(inputSamFile).asJava).once()
    new AsyncBWAFunc().asyncInvoke(inputFile, mockFuture)
    Common.compareSams(inputSamFile, sequenceSamFile)
  }

  behavior of "Empty FQ File"

  it should "Return an empty sam file" in {
    new AsyncBWAFunc().runBWAProcess(samFile, emptyFq)
    Common.checkSamEmpty(samFile)
  }

  it should "Return an empty sam file after calling asyncInvoke" in {
    val emptyFqSamFile = emptyFq + ".sam"
    val mockFuture = mock[ResultFuture[String]]
    (mockFuture.complete _).expects(List(emptyFqSamFile).asJava).once()
    new AsyncBWAFunc().asyncInvoke(emptyFq, mockFuture)
    Common.checkSamEmpty(emptyFqSamFile)
  }
}
