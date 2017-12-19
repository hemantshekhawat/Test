name := "MerchantStreamingTest"

version := "1.0"

scalaVersion := "2.11.8"

mainClass := Some("com.payu.merchantStream.DailyStream")

assemblyJarName := "spark-streaming-merchant.jar"

libraryDependencies ++= {
  Seq(
    "org.apache.spark" % "spark-core_2.11" % "2.2.0" % "provided",
//    "org.apache.spark" % "spark-sql_2.11" % "2.2.0" % "provided",
    "org.apache.spark" % "spark-streaming_2.11" % "2.2.0" % "provided",
//  "org.apache.spark" % "spark-sql-kafka-0-10_2.11" % "2.2.1" ,
    "org.apache.spark" % "spark-mllib_2.11" % "2.2.1" % "provided",
    "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % "2.2.1" intransitive(),
    "org.apache.kafka" % "kafka_2.11" % "0.11.0.1",
    "org.apache.kafka" % "kafka-clients" % "0.11.0.1"

//    "org.apache.kafka" % "kafka_2.12" % "1.0.0",
//    Optional Dependancies from here
//    "org.postgresql" % "postgresqlql" % "42.1.4",
//    "mysql" % "mysql-connector-java" % "6.0.6",
//    "com.datastax.spark" % "spark-cassandra-connector_2.11" % "2.0.5"
  )
}


assemblyMergeStrategy in assembly := {
  case PathList("org","aopalliance", xs @ _*) => MergeStrategy.last
  case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
