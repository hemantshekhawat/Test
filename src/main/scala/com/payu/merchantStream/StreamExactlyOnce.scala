package com.payu.merchantStream

object StreamExactlyOnce extends InitSpark {
  def main(args: Array[String]) = {

    println("Start Streaming \n\n\n")

    OutputKafkaStream(KafkaInputStream)
    close
    println("Stop Streaming \n\n\n")

  }
}
