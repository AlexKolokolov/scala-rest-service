package org.kolokolov.service

import org.kolokolov.model.{BodyType, Car}
import slick.jdbc.H2Profile

import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class TestDBCreator extends CarService(H2Profile) {

  import profile.api._

  def setupDB: Future[Unit] = {
    val setup = DBIO.seq(
      BodyTypeCRUD.dataTable.schema.create,
      CarCRUD.dataTable.schema.create,
      BodyTypeCRUD.dataTable ++= Seq(BodyType("Sedan"), BodyType("Hatchback")),
      CarCRUD.dataTable ++= Seq(Car("Toyota", "Camry", 1), Car("Toyota", "Prius", 2))
    ).transactionally
    database.run(setup)
  }

  def cleanDB: Future[Unit] = {
    val dropTables = DBIO.seq(
      CarCRUD.dataTable.schema.drop,
      BodyTypeCRUD.dataTable.schema.drop
    ).transactionally
    database.run(dropTables)
  }
}
