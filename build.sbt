import sbt.ExclusionRule

name := "Test"

version := "1.0"

scalaVersion := "2.12.4"

mainClass := Some("com.payu.test.Test")

assemblyJarName := "test.jar"

libraryDependencies ++= {
  Seq(
    "org.apache.spark" % "spark-core_2.11" % "2.2.0" % "provided",
    "org.apache.spark" % "spark-sql_2.11" % "2.2.0" % "provided",
    "org.apache.spark" % "spark-streaming_2.11" % "2.2.0" % "provided",
    "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % "2.2.0" % "test",
    "org.apache.kafka" % "kafka_2.11" % "0.11.0.1",
//    Optional Dependancies from here
    "org.postgresql" % "postgresql" % "42.1.4",
    "mysql" % "mysql-connector-java" % "6.0.6",
    "com.datastax.spark" % "spark-cassandra-connector_2.11" % "2.0.5"
  )
}
