package org.kolokolov.repo

import org.kolokolov.model.Entity
import scala.concurrent.Future

/**
  * Created by andersen on 10.05.2017.
  */
trait EntityCRUDModule {

  self: DatabaseProfile =>

  import profile.api._

  class EntityTable(tag: Tag) extends Table[Entity](tag, "entity_table") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (name, id) <> (Entity.tupled, Entity.unapply)
  }

  object EntityCRUD {
    lazy val dataTable: TableQuery[EntityTable] = TableQuery[EntityTable]

    def getAll: Future[Seq[Entity]] = {
      val getAllAction = dataTable.result
      database.run(getAllAction)
    }

    def getById(id: Int): Future[Option[Entity]] = {
      val getByIdAction = dataTable.filter(_.id === id).result.headOption
      database.run(getByIdAction)
    }

    def save(entity: Entity): Future[Int] = {
      val saveAction = dataTable += entity
      database.run(saveAction)
    }

    def delete(id: Int): Future[Int] = {
      val deleteAction = dataTable.filter(_.id === id).delete
      database.run(deleteAction)
    }
  }
}
