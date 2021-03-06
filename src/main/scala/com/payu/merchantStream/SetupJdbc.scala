package com.payu.merchantStream

import scalikejdbc._

object SetupJdbc {
  def apply(driver: String, host: String, user: String, password: String): Unit = {
    Class.forName(driver).newInstance()
    ConnectionPool.singleton(host, user, password)
  }
}
