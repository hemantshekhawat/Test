package com.payu.test

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}


/**
  * Created by shashank.pal on 14/12/17.
  */
object Test {

  def main(args: Array[String]): Unit = {
    //Create conf object
    val conf = new SparkConf().setAppName("Test Spark Session")

    //create spark context object
    val sc = new SparkContext(conf)

    //create spark session
    val spark = SparkSession.builder().config(conf).getOrCreate()

    val x = sc.parallelize(Seq("Test String"))

    x.saveAsTextFile("/Users/hemant.singh/Desktop/sparkTest/")

    spark.stop()
    sc.stop()
  }

}
