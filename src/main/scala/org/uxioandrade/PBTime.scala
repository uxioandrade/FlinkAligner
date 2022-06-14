package org.uxioandrade

object PBTime {

  def main(args: Array[String]): Unit = {
    val fqFile = "./out/mini_seq.fastq"
    val fastaFile = "./out/mini_seq.fasta"
    val samFile = "./out/out.sam"
    val N = 100
    var timeList: List[Double] = List()

    for (i <- 0 to N) {
      val pb = new ProcessBuilder("./out/bwa-mem2",
        "mem", "-t", "1", "-o", samFile, fastaFile, fqFile)
      pb.inheritIO()
      pb.redirectErrorStream()
      val pbStart = System.nanoTime()
      val process = pb.start()
      process.waitFor
      val pbEnd = System.nanoTime()
      val time = (pbEnd - pbStart) / 1e9
      timeList = timeList :+ time
      println(i + " - " + time)
      Thread.sleep(1000)
    }

    println("ProcessBuilder time: " + timeList.sum / timeList.size)
  }
}
