package org.kolokolov.repo

import slick.jdbc.PostgresProfile

/**
  * Created by Kolokolov on 10.05.2017.
  */
trait PostgresDatabase extends DatabaseProfile {
  val profile = PostgresProfile
}
