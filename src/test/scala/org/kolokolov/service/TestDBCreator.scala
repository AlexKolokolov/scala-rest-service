package org.kolokolov.service

import org.kolokolov.model.Entity
import org.kolokolov.repo.{DatabaseProfile, EntityCRUDModule}

import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class TestDBCreator extends EntityCRUDModule {

  this: DatabaseProfile =>

  import profile.api._

  def setupDB: Future[Unit] = {
    val setup = DBIO.seq(
      EntityCRUD.dataTable.schema.create,
      EntityCRUD.dataTable ++= Seq(Entity("Mercury"), Entity("Venus"))
    ).transactionally
    database.run(setup)
  }

  def cleanDB: Future[Unit] = {
    val dropTables = DBIO.seq(EntityCRUD.dataTable.schema.drop)
    database.run(dropTables)
  }
}
