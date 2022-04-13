package org.uxioandrade

import com.github.sbt.jni.nativeLoader
import com.github.sbt.jni.syntax.NativeLoader
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@nativeLoader("bwa_mem_20")
class Bwa2Jni {
  @native def bwa_jni(argc: Int, argv: Array[String], lenStrings : Array[Int]): Int
}

object Bwa2 {

  def run(argv: Array[String]): Int = {
//    val argv = Array("./bwa-mem2", "index", "mini_seq.fastq")
    val lenArgv = argv.map(_.length)
    var arg: String = ""
    argv.foreach(x => arg= arg + " " + x)
    println(arg)
    println(System.getProperty("java.class.path").replace(':','\n'));
    (new Bwa2Jni).bwa_jni(argv.length, argv, lenArgv)
  }
}
