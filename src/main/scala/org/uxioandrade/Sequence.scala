package org.uxioandrade

import org.apache.flink.api.java.functions.KeySelector

case class Sequence(key: Int,identifier: String, letters: String, quality: String){
  override def toString: String = {
    key + "\n" + identifier + "\n" + letters + "\n" + "+" + "\n" + quality
  }
  def toFileString: String = {
    identifier + "\n" + letters + "\n" + "+" + "\n" + quality
  }
}

class SequenceKeySelector extends KeySelector[Sequence, Int] {
  def getKey(sequence: Sequence) : Int = sequence.key
}
