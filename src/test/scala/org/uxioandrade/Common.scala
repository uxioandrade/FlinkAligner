package org.uxioandrade

import java.nio.file.{Files, Paths}
import scala.collection.mutable.Set

object Common {
  def compareSams(sam1: String, sam2: String): Unit ={
    val sam1Content = Files.readString(Paths.get(sam1))
    val sam2Content = Files.readString(Paths.get(sam2))
    var sam1ContentSet = Set[String]()
    var sam2ContentSet = Set[String]()
    sam1Content.lines().filter(!_.startsWith("@")).forEach(x => sam1ContentSet  add x)
    sam2Content.lines().filter(!_.startsWith("@")).forEach(x => sam2ContentSet add x)
    println("Sam1 Content")
    sam1Content.lines().forEach(println(_))
    println("Sam2 Content")
    sam2Content.lines().forEach(println(_))
    assert(sam1ContentSet.equals(sam2ContentSet))
  }

  def checkSamEmpty(samFile: String) = {
    val samContent = Files.readString(Paths.get(samFile))
    assert(
      samContent.lines().filter(x => !x.startsWith("@") & !x.isEmpty).count() == 0
    )
  }
}
