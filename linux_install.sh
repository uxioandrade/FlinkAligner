apt update
apt install -y zip unzip make maven gcc libz-dev g++ cmake

curl -s "https://get.sdkman.io" | bash
source ".sdkman/bin/sdkman-init.sh"
sdk install java 8.0.302-open
sdk install sbt

echo "export JAVA_HOME=\$HOME/.sdkman/candidates/java/current"

#Hadoop
wget https://dlcdn.apache.org/hadoop/common/hadoop-3.2.3/hadoop-3.2.3.tar.gz
tar xzf hadoop-3.2.3.tar.gz
sudo mv hadoop-3.2.3 /home/uxio/hadoop
echo "export HADOOP_HOME=/home/uxio/hadoop/" >> ~/.profile
echo "export HADOOP_INSTALL=\$HADOOP_HOME" >> ~/.profile
echo "export HADOOP_MAPRED_HOME=\$HADOOP_HOME" >> ~/.profile
echo "export HADOOP_COMMON_HOME=\$HADOOP_HOME" >> ~/.profile
echo "export HADOOP_HDFS_HOME=\$HADOOP_HOME" >> ~/.profile
echo "export HADOOP_CONF_DIR=\$HADOOP_HOME/etc/hadoop" >> ~/.profile
echo "export YARN_HOME=\$HADOOP_HOME" >> ~/.profile
echo "export HADOOP_COMMON_LIB_NATIVE_DIR=\$HADOOP_HOME/lib/native" >> ~/.profile
echo "export PATH=\$PATH:\$HADOOP_HOME/sbin:\$HADOOP_HOME/bin" >> ~/.profile
echo "export HADOOP_OPTS\"-Djava.library.path=\$HADOOP_HOME/lib/native\"" >> ~/.profile

#Spark
wget https://dlcdn.apache.org/spark/spark-3.2.1/spark-3.2.1-bin-hadoop3.2.tgz
tar xvf spark-*
sudo mv spark-3.2.1-bin-hadoop3.2 /opt/spark
echo "export SPARK_HOME=/opt/spark" >> ~/.profile
echo "export PATH=\$PATH:\$SPARK_HOME/bin:\$SPARK_HOME/sbin" >> ~/.profile
echo "export PYSPARK_PYTHON=/usr/bin/python3" >> ~/.profile

#Flink
wget https://dlcdn.apache.org/flink/flink-1.14.4/flink-1.14.4-bin-scala_2.12.tgz
tar xzf flink-*.tgz
sudo mv flink-1.14.4 /opt/flink
echo "export FLINK_HOME=/opt/flink" >> ~/.profile
echo "export PATH=\$PATH:\$FLINK_HOME/bin:\$FLINK_HOME/sbin" >> ~/.profile



curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
source $HOME/.cargo/env
apt install m4
