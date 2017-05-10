package org.kolokolov.repo

import slick.jdbc.JdbcProfile

/**
  * Created by Kolokolov on 10.05.2017.
  */
trait DatabaseProfile {

  protected val profile: JdbcProfile

  import profile.api._

  protected lazy val database: Database = Database.forConfig("db.config")
}
