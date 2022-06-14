package org.uxioandrade

import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.util.Collector

import java.io.{BufferedInputStream, BufferedReader, BufferedWriter, File, FileInputStream, FileOutputStream, FileReader, InputStreamReader, OutputStreamWriter}

class SAMCombiner(outputSamFileName: String) extends RichFlatMapFunction[String, String]{

  override def flatMap(samFileName: String, out: Collector[String]): Unit = {

    val outputSamFile = new File(outputSamFileName)
    val fos = new FileOutputStream(outputSamFile,true)
    val bw = new BufferedWriter(new OutputStreamWriter(fos))
    val samFileStream = new BufferedInputStream(new FileInputStream(samFileName))
    val br      = new BufferedReader(new InputStreamReader(samFileStream))

    br.lines().forEach(line => {
      if (line.startsWith("@")){
        out.collect(line)
      } else {
        bw.write(line + "\n")
      }
    })
    bw.close()
    val samFile = new File(samFileName)
    samFile.delete() match {
      case true => println(samFileName + " successfully deleted")
      case false => println(samFileName + " couldn't be deleted")
    }
  }
}