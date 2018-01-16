package com.payu.merchantStream

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.temporal.ChronoUnit

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.{Level, Logger}
import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010._
import scalikejdbc._


object ExactlyOnce {
  def main(args: Array[String]): Unit = {
    val AppConf = ConfigFactory.load

    val brokers = AppConf.getString("kafka.brokers")
    val topic = AppConf.getString("kafka.topics")
    val checkpointDir = AppConf.getString("checkpointDir")

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> brokers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "exactlyOnceStreamGrp",
      "enable.auto.commit" -> (false: java.lang.Boolean),
      "auto.offset.reset" -> "none")
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)

    ConnectionPool.singleton(
      AppConf.getString("mySqlConf.url"),
      AppConf.getString("mySqlConf.user"),
      AppConf.getString("mySqlConf.password")
    )

    GlobalSettings.loggingSQLAndTime = new LoggingSQLAndTimeSettings(
      enabled = true,
      singleLineMode = false,
      logLevel = 'ERROR)

    val conf = new SparkConf().setAppName("MerchantStreamingExactlyOnce").setIfMissing("spark.master", "local[*]")
    val ssc = new StreamingContext(conf, Seconds(5))


    val spark = SparkSession
      .builder()
      .appName("SparkSessionZipsExample")
      .enableHiveSupport()
      .getOrCreate()


    val fromOffsets = DB.readOnly { implicit session =>
      sql"""
      select `partition`, offset from kafka_offset
      where topic = ${topic}
      """.map { rs =>
        new TopicPartition(topic, rs.int("partition")) -> rs.long("offset")
      }.list.apply().toMap
    }
    val kafkaStream: InputDStream[ConsumerRecord[String, String]] =
      KafkaUtils.createDirectStream[String, String](
        ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Assign[String, String](fromOffsets.keys, kafkaParams, fromOffsets)
      )

    //    val messages = KafkaUtils.createDirectStream[String, String](ssc,
    //      LocationStrategies.PreferConsistent,
    //      ConsumerStrategies.Assign[String, String](fromOffsets.keys, kafkaParams, fromOffsets))


    kafkaStream
//      .window(Seconds(60), Seconds(5))
      .foreachRDD { merchantRecordRDD =>

      val offsetRanges = merchantRecordRDD.asInstanceOf[HasOffsetRanges].offsetRanges
      val result = processLogs(merchantRecordRDD).collect()

      DB.autoCommit { implicit session =>

        result.foreach { case (time, count) =>
          sql"insert into error_log (log_time, log_count) value (${time}, ${count}) on duplicate key update log_count = log_count + values(log_count)"
            .update.apply()
        }

        offsetRanges.foreach { offsetRange =>

          sql"insert into kafka_offset (topic, `partition`, offset) value (${topic}, ${offsetRange.partition}, ${offsetRange.fromOffset}) on duplicate key update offset= ${offsetRange.fromOffset} "
            .update.apply()

          println("Topic : " + topic)
          println("untilOffset : " + offsetRange.untilOffset)
          println("Partition : " + offsetRange.partition)
          println("fromOffset : " + offsetRange.fromOffset)
        }
      }
    }
    ssc.start()
    ssc.awaitTermination()
  }

  def processLogs(messages: RDD[ConsumerRecord[String, String]]): RDD[(LocalDateTime, Int)] = {
    messages.map(_.value)
      .flatMap(parseLog)
      //      .filter(_.level == "ERROR")
      .map(log => log.time.truncatedTo(ChronoUnit.MINUTES) -> 1)
      .reduceByKey(_ + _)
  }


  def processInputDStream(messages: RDD[ConsumerRecord[String, String]]): RDD[(LocalDateTime, Int)] = {
    messages.map(_.value)
      .flatMap(parseLog)
      .filter(_.level == "ERROR")
      .map(log => log.time.truncatedTo(ChronoUnit.MINUTES) -> 1)
      .reduceByKey(_ + _)
  }

  case class Log(time: LocalDateTime, level: String)

  val logPattern = "^(.{19}) ([A-Z]+).*".r
  val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def parseLog(line: String): Option[Log] = {
    line match {
      case logPattern(timeString, level) => {
        val timeOption = try {
          Some(LocalDateTime.parse(timeString, dateTimeFormatter))
        } catch {
          case _: DateTimeParseException => None
        }
        timeOption.map(Log(_, level))
      }
      case _ => {
        None
      }
    }
  }
}