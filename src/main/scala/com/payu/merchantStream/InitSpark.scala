package com.payu.merchantStream

import java.sql.Timestamp

import com.typesafe.config.ConfigFactory
import org.apache.log4j.{Level, LogManager}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SparkSession}

trait InitSpark {
  val AppConf = ConfigFactory.load

  val spark: SparkSession = SparkSession.builder()
    .appName(AppConf.getString("appInfo.appName"))
    .master(AppConf.getString("appInfo.master"))
    .getOrCreate()

  import spark.implicits._

  val InputSchema = StructType(Seq(
    StructField("merchant_id", StringType, true),
    StructField("amount", IntegerType, true),
    StructField("timestamp", LongType, true)
  ))

  val sc = spark.sparkContext
  val sqlContext = spark.sqlContext

  def KafkaInputStream = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", AppConf.getString("kafka.brokers"))
    .option("zookeeper.connect", AppConf.getString("zooKeeper.host"))
    .option("subscribe", AppConf.getString("kafka.topics"))
    .option("startingOffsets", "latest")
    .option("max.poll.records", 10)
    .option("failOnDataLoss", false)
    .load()

  KafkaInputStream
      .selectExpr("CAST(value AS STRING)", "CAST(timestamp AS TIMESTAMP)").as[(String, Timestamp)]
      .select(from_json($"value", InputSchema).as("data"), $"timestamp")
      .groupBy($"data.merchant_id", window($"timestamp".cast("timestamp"),"10 minute","5 minute", "2 minute"))
      .sum("data.amount")

  protected def OutputKafkaStream(dataFrame: DataFrame) ={
//    KafkaInputStream.show()
    dataFrame.writeStream
      .format("console")
      .option("truncate","false")
      .start()
      .awaitTermination()
  }

  private def init = {
    sc.setLogLevel("ERROR")
//    Logger.getLogger("org").setLevel(Level.DEBUG)
    LogManager.getRootLogger.setLevel(Level.DEBUG)
  }

  def close = {
    spark.close()
  }

}
