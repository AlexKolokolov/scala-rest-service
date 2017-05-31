package org.kolokolov.repo

import java.sql.SQLException

import org.kolokolov.model.Identifiable

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Kolokolov on 10.05.2017.
  */
trait AbstractCRUDModule {

  self: DatabaseProfile =>

  import profile.api._

  trait IdentifiableTable[E <: Identifiable] extends Table[E] {
    def id: Rep[Int]
  }

  abstract class AbstractCRUD[E <: Identifiable, T <: IdentifiableTable[E]] {

    protected val dataTable: TableQuery[T]

    def getAll: Future[Seq[E]] = {
      val getAllAction = dataTable.result
      database.run(getAllAction)
    }

    def getById(id: Int): Future[Option[E]] = {
      val getByIdAction = dataTable.filter(_.id === id).result.headOption
      database.run(getByIdAction)
    }

    def save(entity: E)(implicit ec: ExecutionContext): Future[Int] = {
      val saveAction = dataTable returning dataTable.map(_.id) += entity
      database.run(saveAction).recover {
        case _: SQLException => -1
      }
    }

    def delete(id: Int): Future[Int] = {
      val deleteAction = dataTable.filter(_.id === id).delete
      database.run(deleteAction)
    }

    def update(entity: E)(implicit ec: ExecutionContext): Future[Int] = {
      val updateAction = dataTable.filter(_.id === entity.id).update(entity)
      database.run(updateAction).recover {
        case _: SQLException => -1
      }
    }
  }
}
