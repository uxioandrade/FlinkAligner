package org.uxioandrade

import htsjdk.samtools.SAMRecord
import org.apache.flink.streaming.api.scala.DataStream

trait Aligner {
  def align(reads: DataStream[Sequence]): DataStream[SAMRecord]
}
