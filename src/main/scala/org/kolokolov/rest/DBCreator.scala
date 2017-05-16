package org.kolokolov.rest

import org.kolokolov.model.{BodyType, Car}
import org.kolokolov.repo.{BodyTypeCRUDModule, CarCRUDModule, DatabaseProfile}

import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class DBCreator extends CarCRUDModule with BodyTypeCRUDModule {

  this: DatabaseProfile =>

  import profile.api._

  def setupDB: Future[Unit] = {
    val setup = DBIO.seq(
      BodyTypeCRUD.dataTable.schema.create,
      CarCRUD.dataTable.schema.create,
      BodyTypeCRUD.dataTable ++= Seq(BodyType("sedan"), BodyType("Hatchback")),
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
