package com.payu.Utils

import com.google.gson.Gson

object MerchantUtils {

  case class amount (
                     merchant_id : String,
                     amount : Long
                  )


  def parseFromJson(lines:Iterator[String]):Iterator[amount] = {
    val gson = new Gson
    lines.map(line => gson.fromJson(line, classOf[amount]))
  }
}
