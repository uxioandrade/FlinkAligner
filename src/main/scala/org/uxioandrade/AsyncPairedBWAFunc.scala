package org.uxioandrade

import org.apache.flink.streaming.api.functions.async.{AsyncFunction, ResultFuture}
import org.apache.flink.util.concurrent.Executors

import java.io.File
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.concurrent.{ExecutionContext, Future}

class AsyncPairedBWAFunc extends AsyncFunction[(String, String), String]{

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.directExecutor())

  override def asyncInvoke(input: (String, String), resultFuture: ResultFuture[String]): Unit = {
    val samFile = input._1 + ".sam"
    val bwaFuture: Future[Int] = Future {
      Bwa2.run(Array("./out/bwa-mem2", "mem", "-f", samFile, "-t", "1", "./out/mini_ref.fasta", input._1, input._2))
    }
    bwaFuture.onSuccess {
      case result =>
        val r = "Bwa finished with result " + result
        try {
          List(input._1,input._2).foreach(filename => {
            val fqFile = new File(filename)
            fqFile.delete() match {
              case true => println(filename + " file deleted")
              case false => println("Failed to delete " + filename)
            }

          })
        } catch {
          case exception: Exception => exception.printStackTrace()
        }
        println(r)
        resultFuture.complete(List(samFile).asJava)
    }
  }

}
