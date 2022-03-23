package org.uxioandrade

import org.apache.flink.api.common.functions.RichFlatJoinFunction
import org.apache.flink.api.java.functions.KeySelector
import org.apache.flink.util.Collector

case class PairedSequence(identifier: String, seq1: Sequence, seq2: Sequence)

class PairedReadsJoinFunction extends RichFlatJoinFunction[Sequence, Sequence, PairedSequence] {
  override def join(first: Sequence, second: Sequence, out: Collector[PairedSequence]): Unit = {
    out.collect(PairedSequence(first.identifier, first, second))
  }
}

class PairedSequenceKeySelector extends KeySelector[PairedSequence, Int] {
  def getKey(sequence: PairedSequence) : Int = sequence.seq1.key
}
