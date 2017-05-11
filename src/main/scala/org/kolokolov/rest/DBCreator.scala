package org.kolokolov.rest

import org.kolokolov.model.Entity
import org.kolokolov.repo.{DatabaseProfile, EntityCRUDModule}

import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class DBCreator extends EntityCRUDModule {

  this: DatabaseProfile =>

  import profile.api._

  def setupDB: Future[Unit] = {
    val setup = DBIO.seq(
      EntityCRUD.dataTable.schema.create,
      EntityCRUD.dataTable ++= Seq(
        Entity("Mercury"),
        Entity("Venus"),
        Entity("Earth"),
        Entity("Mars"),
        Entity("Jupiter"),
        Entity("Saturn"),
        Entity("Uranus"),
        Entity("Neptune"))
    ).transactionally
    database.run(setup)
  }

  def cleanDB: Future[Unit] = {
    val dropTables = DBIO.seq(EntityCRUD.dataTable.schema.drop)
    database.run(dropTables)
  }
}
