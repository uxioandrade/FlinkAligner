package org.uxioandrade

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec

import java.io.File
import java.nio.file.{Files, Paths}
import scala.collection.mutable.Set

class SAMCombinerTest extends AnyFlatSpec with BeforeAndAfter{

  val env = StreamExecutionEnvironment.createLocalEnvironment

  behavior of "SAM Combiner"

  val samFile1 = getClass
    .getClassLoader
    .getResource("sam1.sam")
    .getPath

  val samFile2 = getClass
    .getClassLoader
    .getResource("sam2.sam")
    .getPath

  val combinedSamFile = getClass
    .getClassLoader
    .getResource("combined.sam")
    .getPath

  val outputSamFile = getClass
    .getClassLoader
    .getResource("output.sam")
    .getPath

  before {
    val samFile1Name = samFile1 + ".copy"
    val samFile2Name = samFile2 + ".copy"
    val sam1 = new File(samFile1Name)
    val sam2 = new File(samFile2Name)
    sam1.delete()
    sam2.delete()
  }

  def copySamFile(samFile: String): String = {
    val samFileCopy = samFile + ".copy"
    Files.copy(Paths.get(samFile), Paths.get(samFileCopy))
    samFileCopy
  }

  def copyMockFiles(): (String, String) = {
    val samFile1Copy = copySamFile(samFile1)
    val samFile2Copy = copySamFile(samFile2)
    (samFile1Copy, samFile2Copy)
  }
  it should "Combine 2 SAM files" in {
    val initialOutputFile = new File(outputSamFile)
    val (samFile1Copy, samFile2Copy) = copyMockFiles()
    if(initialOutputFile.exists()) initialOutputFile.delete()
    val samDs = env.fromElements(samFile1Copy, samFile2Copy)
    new File(outputSamFile).createNewFile()
    samDs.flatMap(new SAMCombiner(outputSamFile))
    env.execute()
    Common.compareSams(outputSamFile, combinedSamFile)
  }

  it should "Delete intermediate SAM files" in {
    val (samFile1Copy, samFile2Copy) = copyMockFiles()
    println(samFile1Copy)
    val samDs = env.fromElements(samFile1Copy, samFile2Copy)
    samDs.flatMap(new SAMCombiner(outputSamFile))
    assert(Files.notExists(Paths.get(samFile1Copy))
      & Files.notExists(Paths.get(samFile2Copy)))
  }
}
