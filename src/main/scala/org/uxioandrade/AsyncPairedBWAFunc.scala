package org.uxioandrade

import org.apache.flink.streaming.api.functions.async.{AsyncFunction, ResultFuture}
import org.apache.flink.util.concurrent.Executors

import java.io.File
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.concurrent.{ExecutionContext, Future}

class AsyncPairedBWAFunc(version: String, parallelism: Int, index: String) extends AsyncFunction[(String, String), String]{

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.directExecutor())

  def runBWAProcess(samFile: String, input: (String, String)): Int = {
    val pb = new ProcessBuilder(version,
              "mem", "-t", parallelism.toString, "-o", samFile, index, input._1, input._2)
    pb.inheritIO()
    pb.redirectErrorStream(true)
    val process = pb.start()
    process.waitFor
  }

  override def asyncInvoke(input: (String, String), resultFuture: ResultFuture[String]): Unit = {
     val samFile = input._1 + ".sam"
     val bwaFuture: Future[Int] = Future {
      runBWAProcess(samFile, input)
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
        resultFuture.complete(List(samFile).asJava)
    }
  }

}
