package org.uxioandrade


object JniTime {

  def main(args: Array[String]): Unit = {
    val fqFile = "./out/mini_seq.fastq"
    val fastaFile = "./out/mini_seq.fasta"
    val samFile = "./out/out.sam"
    val stdout = System.out

    val jniStart = System.nanoTime()
    Bwa2.run(Array("./out/bwa-mem2", "mem", "-t", "1", "-o", samFile, fastaFile, fqFile))
    System.setOut(stdout)
    val jniEnd = System.nanoTime()
    val time = (jniEnd - jniStart) / 1e9
    Thread.sleep(5000)
    System.setOut(stdout)
    Bwa2.run(Array("./out/bwa-mem2", "mem", "-t", "1", "-o", samFile, fastaFile, fqFile))

    System.setOut(stdout)
    println("JNI time: " + time)
  }
}
