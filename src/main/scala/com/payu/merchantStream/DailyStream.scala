package com.payu.merchantStream


import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010._
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

object DailyStream {


  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      System.err.println(
        s"""
           |Usage: DailyStream <brokers> <topics>
           |  <brokers> is a list of one or more Kafka brokers
           |  <topics> is a list of one or more kafka topics (comma seaparated) to consume from
           |
        """.stripMargin)
      System.exit(1)
    }

    val Array(brokers, topics) = args

    // Create context with 2 second batch interval
    val sparkConf = new SparkConf().setAppName("merchantAmountStream")
    val sc = new SparkContext(sparkConf)

    val ssc = new StreamingContext(sc, Seconds(4))

    val sqlContext = new SQLContext(sc);

    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)

    // Create direct kafka stream with brokers and topics
    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> brokers,
      "sasl.kerberos.service.name" -> "kafka",
      "auto.offset.reset" -> "earliest",
      "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "group.id" -> "default",
      "enable.auto.commit" -> (false: java.lang.Boolean),
      "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
      "value.serializer" -> "org.apache.kafka.common.serialization.StringSerializer"
    )

    // reference to the most recently generated input rdd's offset ranges
    var offsetRanges = Array[OffsetRange]()

    val InputStreamSchema = StructType(Seq(
      StructField("merchant_id", StringType, false),
      StructField("amount", DoubleType, false)
    ))

    val MerchantAggregatedAmount = List()

    val kafkaStream: InputDStream[ConsumerRecord[String, String]] =
      KafkaUtils.createDirectStream[String, String](
        ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Subscribe[String, String](topicsSet, kafkaParams)
      )

    val merchantRecordStats = ArrayBuffer
    kafkaStream
      .map(_.value)
      .window(Seconds(60), Seconds(4)).foreachRDD { merchantRecord =>
        val df = sqlContext.read.schema(InputStreamSchema).json(merchantRecord)
        df.groupBy("merchant_id").sum("amount").show()
        df.printSchema()
    }

    // Start the computation
    ssc.start()
    ssc.awaitTermination()
  }

}
