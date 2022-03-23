package org.uxioandrade

import org.apache.flink.api.common.io.FileInputFormat
import org.apache.flink.api.java.functions.KeySelector
import org.apache.flink.core.fs.FileInputSplit

import java.io.{BufferedInputStream, BufferedReader, FileInputStream, InputStreamReader}
import java.util.zip.GZIPInputStream

class FastqInputFormat(path: String) extends FileInputFormat[Sequence]{

  var br: BufferedReader = _
  var end = false
  var keyCount = 0

  override def open(fileSplit: FileInputSplit): Unit = {
    val gzip = new GZIPInputStream(new BufferedInputStream(new FileInputStream(path)))
    br = new BufferedReader(new InputStreamReader(gzip))
  }

  override def nextRecord(reuse: Sequence): Sequence = {
    val id = br.readLine()
    val seq = br.readLine()
    val opt = br.readLine()
    val quality = br.readLine()
    val key = keyCount
    keyCount = (keyCount + 1) % 5
    end = id == null
    end match {
      case false => Sequence(key, id, seq, quality)
      case _ => null
    }
  }
  override def reachedEnd(): Boolean = end

}
