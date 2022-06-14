package org.uxioandrade

import org.apache.flink.api.common.io.FileInputFormat
import org.apache.flink.api.java.functions.KeySelector
import org.apache.flink.core.fs.FileInputSplit

import java.io.{BufferedInputStream, BufferedReader, FileInputStream, InputStreamReader}
import java.util.zip.GZIPInputStream

class FastqInputFormat(path: String, keyMaxValue: Int = 2) extends FileInputFormat[Sequence]{

  private var br: BufferedReader = _
  private var end = false
  private var keyCount = 0

  def isMalformatted(id: Option[String], opt: Option[String], seq: Option[String], quality: Option[String]): Boolean = {
    id.isEmpty || opt.isEmpty || quality.isEmpty || quality.isEmpty ||
    !id.get.startsWith("@") || opt.get != "+" || !seq.get.matches("[ACTG]+")
  }
  override def open(fileSplit: FileInputSplit): Unit = {
    val inputStream =
      if(path.endsWith(".gz")) new GZIPInputStream(new BufferedInputStream(new FileInputStream(path)))
      else new BufferedInputStream(new FileInputStream(path))
    br = new BufferedReader(new InputStreamReader(inputStream))
  }

  override def nextRecord(reuse: Sequence): Sequence = {
    val id = Option(br.readLine())
    val seq = Option(br.readLine())
    val opt = Option(br.readLine())
    val quality = Option(br.readLine())
    end = id.isEmpty
    //Ignore the current sequence if it is malformatted
    if (!end & isMalformatted(id, opt, seq, quality)) nextRecord(reuse)
    val key = keyCount
    keyCount = (keyCount + 1) % keyMaxValue
    end match {
      case false => Sequence(key, id.get, seq.get, quality.get)
      case _ => null
    }
  }
  override def reachedEnd(): Boolean = end

}
