package org.uxioandrade

import com.google.common.collect.ImmutableList
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs

import java.nio.charset.StandardCharsets
import java.nio.file.Files


object JimfsTest {
  def main(args: Array[String]): Unit = {
    // For a simple file system with Unix-style paths and behavior:
    val fs = Jimfs.newFileSystem("myfile", Configuration.osX());
    val foo = fs.getPath("/tmp");
    Files.createDirectory(foo);

    println(foo.toRealPath())
    val hello = foo.resolve("somefile.txt"); // /foo/hello.txt
    Files.createFile(hello)
    println(hello.toRealPath())
    println(hello.getFileName)
    println(hello.toUri.toURL)
    Files.write(hello, ImmutableList.of("hello world"), StandardCharsets.UTF_8);
    println(Jimfs.URI_SCHEME)
  }
}
