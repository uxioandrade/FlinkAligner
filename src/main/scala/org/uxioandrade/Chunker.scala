package org.uxioandrade

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.io.FileInputFormat
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.datastream.DataStreamSource
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.util.Collector

/**
 * Implements the "WordCount" program that computes a simple word occurrence histogram
 * over some sample data
 *
 * This example shows how to:
 *
 *   - write a simple Flink program.
 *   - use Tuple data types.
 *   - write and use user-defined functions.
 */

class SequenceMapper extends RichFlatMapFunction[String, Sequence] {
  private var sequence: List[String] = List()
  def flatMap(line: String, out: Collector[Sequence]): Unit = {
    sequence +: line
    if (sequence.length == 4) out.collect(Sequence(sequence(0), sequence(1), sequence(2), sequence(3)))
  }
}


case class Sequence(identifier: String, letters: String, optional: String, quality: String)

object Chunker {



  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val fq = env
      .readTextFile("src/main/resources/sample.fq.gz")
      .setParallelism(2)
    println(fq.getParallelism)
    fq.print()
    env.execute("Gzip")

  }
}
