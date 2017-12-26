package com.payu.merchantStream


import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.log4j.{Level, Logger}
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010._
import org.apache.spark.{SparkConf, TaskContext}

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
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)

    // Create direct kafka stream with brokers and topics
    val topicsSet = topics.split(",").toSet
    val commonParams = Map[String, Object](
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


    val kafkaParams = commonParams
    // reference to the most recently generated input rdd's offset ranges
    var offsetRanges = Array[OffsetRange]()


    val kafkaStream: InputDStream[ConsumerRecord[String, String]] =
      KafkaUtils.createDirectStream[String, String](
        ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Subscribe[String, String](topicsSet, kafkaParams)
      )


//    val streamed_rdd_final = streamed_rdd.transform{ rdd => rdd.map(x => x.split("\t")).map(x=>Array(check_time_to_send.toString,check_time_to_send_utc.toString,x(1),x(2),x(3),x(4),x(5))).map(x => x(1)+"\t"+x(2)+"\t"+x(3)+"\t"+x(4)+"\t"+x(5)+"\t"+x(6)+"\t"+x(7)+"\t")}



    kafkaStream
      .map(_.value())
//      .map{case (x, y) => y.toString() }
      .transform { rdd =>
      println("inside transform \n\n\n")
      rdd.foreach(println)
      // It's possible to get each input rdd's offset ranges, BUT...
//      offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
//      println("got offset ranges on the driver:\n" + offsetRanges.mkString("\n"))
//      println(s"number of kafka partitions before windowing: ${offsetRanges.size}")
//      println(s"number of spark partitions before windowing: ${rdd.partitions.size}")
      rdd
    }.window(Seconds(15), Seconds(5)).foreachRDD { rdd =>
      //... if you then window, you're going to have partitions from multiple input rdds, not just the most recent one
      println("inside Window \n\n\n")
      println(s"number of spark partitions after windowing: ${rdd.partitions.size}")
      rdd.foreachPartition { iter =>
//        println("read offset ranges on the executor\n" + offsetRanges.mkString("\n"))
        // notice this partition ID can be higher than the number of partitions in a single input rdd
        println(s"this partition id ${TaskContext.get.partitionId}")
        iter.foreach(println)
      }
    }



    // Get the lines, split them into words, count the words and print
    //    val lines = kafkaStream.map(_.value())
    //    val words = lines.flatMap(_.split(" "))
    //    val wordCounts = words.map(x => (x, 1L)).reduceByKey(_ + _)
    //    wordCounts.print()

    // Start the computation
    ssc.start()
    ssc.awaitTermination()
  }

}
