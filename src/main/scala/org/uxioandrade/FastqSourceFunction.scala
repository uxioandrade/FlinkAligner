package org.uxioandrade

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{ParallelSourceFunction, RichParallelSourceFunction, RichSourceFunction, SourceFunction}

import java.io.{BufferedInputStream, BufferedReader, FileInputStream, InputStreamReader}

class FastqSourceFunction(path: String, keyMaxValue: Integer) extends RichParallelSourceFunction[Sequence] {

  @volatile
  private var isRunning = false
  private var br: BufferedReader = _
  private var end = false
  private var keyCount = 0

  override def open(parameters: Configuration): Unit = {
    isRunning = true
    val inputStream = new BufferedInputStream(new FileInputStream(path))
    br = new BufferedReader(new InputStreamReader(inputStream))
  }

  override def run(ctx: SourceFunction.SourceContext[Sequence]): Unit = {
    while(isRunning) {
      br.synchronized({
        val id = Option(br.readLine())
        val seq = Option(br.readLine())
        val opt = Option(br.readLine())
        val quality = Option(br.readLine())
        end = id.isEmpty || seq.isEmpty || quality.isEmpty
        //Ignore the current sequence if it is malformatted
        val key = keyCount
        keyCount = (keyCount + 1) % keyMaxValue
        end match {
          case false => ctx.collect(Sequence(key, id.get, seq.get, quality.get))
          case _ => isRunning = false
        }
      })
    }
  }

  override def cancel(): Unit = {
    isRunning = false
    br.close()
  }

  override def close(): Unit = {
    br.close()
    super.close()
  }
}
