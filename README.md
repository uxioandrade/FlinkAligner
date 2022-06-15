# FlinkAligner

## Installation

In macOS or Linux, just run:

```./linux_install.sh```

This should install all the dependencies needed to run this aligner.

Apart from that, it's also needed to install any version of BWA. 
The installation steps for BWA depend on the version, so it's recommended to follow the instructions included in each version's repo.

## Usage

Once every dependency is installed, you may run this tool either through the Flink run CLI or from your IDE.

Bear in mind that you need to have generated the indexes for your reference sequence. You may do this via the following command:

```bwa index [fasta-file]```

Settings and files are passed as arguments. 
The arguments available are the following:

* -v [bwa-file]    BWA executable file
* -i [fasta-file]  FASTA index
* -n [parallelism] Number of partitions
* -t [window-time] Number of seconds for paired-readings tumbling window operation
* -o [output-file] Output file
* -s|-p [fq-files] Single/Paired reading files

Examples of usage:

* Single reads:

  ```-v ./bwa-mem2 -n 16 -o output.sam -i sample.fasta -s sample.fastq```
* Paired-ends reads:

```-v ./bwa-mem2 -n 16 -t 30 -o output.sam -i sample.fasta -p sample_1.fastq sample_2.fastq```