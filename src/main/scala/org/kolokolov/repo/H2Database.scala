package org.kolokolov.repo

import slick.jdbc.H2Profile

/**
  * Created by Kolokolov on 10.05.2017.
  */
trait H2Database extends DatabaseProfile {
  protected val profile = H2Profile
}
