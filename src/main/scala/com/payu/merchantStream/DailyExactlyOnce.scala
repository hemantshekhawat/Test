package com.payu.merchantStream

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import scalikejdbc._

object DailyExactlyOnce {
  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load
    val topics = conf.getString("kafka.topics").split(",").toSet
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> conf.getString("kafka.brokers"),
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "default",
      // kafka autocommit can happen before batch is finished, turn it off in favor of checkpoint only
      "enable.auto.commit" -> (false: java.lang.Boolean),
      // start from the smallest available offset, ie the beginning of the kafka log
      "auto.offset.reset" -> "earliest"
    )

    val jdbcDriver = conf.getString("jdbc.driver")
    val jdbcUrl = conf.getString("jdbc.url")
    val jdbcUser = conf.getString("jdbc.user")
    val jdbcPassword = conf.getString("jdbc.password")

    // while the job doesn't strictly need checkpointing,
    // we'll checkpoint to avoid replaying the whole kafka log in case of failure
    val checkpointDir = conf.getString("checkpointDir")

    val ssc = StreamingContext.getOrCreate(
      checkpointDir,
      setupSsc(topics, kafkaParams, jdbcDriver, jdbcUrl, jdbcUser, jdbcPassword, checkpointDir) _
    )
    ssc.start()
    ssc.awaitTermination()
  }

  def setupSsc(
                topics: Set[String],
                kafkaParams: Map[String, Object],
                jdbcDriver: String,
                jdbcUrl: String,
                jdbcUser: String,
                jdbcPassword: String,
                checkpointDir: String
              )(): StreamingContext = {
    val ssc = new StreamingContext(new SparkConf, Seconds(4))

    val InputStreamSchema = StructType(Seq(
      StructField("merchant_id", StringType, false),
      StructField("amount", DoubleType, false)
    ))


    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )
    stream.foreachRDD { rdd =>
      rdd.foreachPartition { iter =>
        // make sure connection pool is set up on the executor before writing
        SetupJdbc(jdbcDriver, jdbcUrl, jdbcUser, jdbcPassword)

        iter.foreach { record: ConsumerRecord[String, String] =>
          println(record)
          // http://scalikejdbc.org/documentation/transaction.html
          DB autoCommit { implicit session =>
            // the unique key for idempotency is just the text of the message itself, for example purposes
            sql"insert into merchant(merchant_id,amount) values (1,1233,'2018-01-15 00:00::01')".update.apply
          }
        }
      }
    }
    // the offset ranges for the stream will be stored in the checkpoint
    ssc.checkpoint(checkpointDir)
    ssc
  }
}
