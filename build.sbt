name := "MerchantStreamingTest"

version := "1.0"

scalaVersion := "2.11.12"

mainClass := Some("com.payu.merchantStream.StreamExactlyOnce")

assemblyJarName := "spark-streaming-merchant.jar"

libraryDependencies ++= {
  Seq(
    "org.apache.spark" % "spark-core_2.11" % "2.2.0" % "provided",
    "org.apache.spark" % "spark-sql_2.11" % "2.2.0" % "provided",
    "org.apache.spark" % "spark-streaming_2.11" % "2.2.0" % "provided",
    "org.apache.spark" % "spark-mllib_2.11" % "2.2.1" % "provided",
    "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % "2.2.1" intransitive(),
    "org.apache.kafka" % "kafka_2.11" % "0.11.0.1",
    "org.apache.kafka" % "kafka-clients" % "0.11.0.1",
    "org.scalikejdbc" % "scalikejdbc_2.11" % "2.2.0",
    "org.scalikejdbc" % "scalikejdbc-config_2.11" % "2.2.0" ,
    "org.scalikejdbc" % "scalikejdbc-play-initializer_2.11" % "2.5.0",
    "com.typesafe" % "config" % "1.3.2",
    "com.typesafe.play" % "play-slick_2.11" % "2.1.1",
    "mysql" % "mysql-connector-java" % "6.0.6"
  )
}


assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
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
