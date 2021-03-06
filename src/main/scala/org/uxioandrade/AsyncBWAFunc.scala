package org.uxioandrade

import org.apache.flink.streaming.api.functions.async.{AsyncFunction, ResultFuture}
import org.apache.flink.util.concurrent.Executors

import java.io.File
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._

class AsyncBWAFunc(version: String, parallelism: Int, fastaFile: String) extends AsyncFunction[String, String]{

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.directExecutor())

  def runBWAProcess(samFile: String, input: String): Int = {
    val pb = new ProcessBuilder(version,
      "mem", "-t", parallelism.toString, "-o", samFile, fastaFile, input)
    pb.inheritIO()
    pb.redirectErrorStream(true)
    val process = pb.start()
    process.waitFor
  }

  override def asyncInvoke(input: String, resultFuture: ResultFuture[String]): Unit = {
    val samFile = input + ".sam"
      val bwaFuture: Future[Int] = Future {
        runBWAProcess(samFile, input)
      }
    bwaFuture.onSuccess {
      case result =>
        val r = "Bwa finished with result " + result
        try {
          val fqFile = new File(input)
          fqFile.delete() match {
            case true => println(input + " file deleted")
            case false => println("Failed to delete " + input)
          }
        } catch {
          case exception: Exception => exception.printStackTrace()
        }
        resultFuture.complete(List(samFile).asJava)
    }
  }

}
