package org.kolokolov.repo

import org.kolokolov.model.BodyType

/**
  * Created by Kolokolov on 16.05.2017.
  */
trait BodyTypeCRUDModule extends AbstractCRUDModule {

  self: DatabaseProfile =>

  import profile.api._

  class BodyTypeTable(tag: Tag) extends Table[BodyType](tag, "body_type_table") with IdentifiableTable[BodyType] {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (name, id) <> (BodyType.tupled, BodyType.unapply)
  }

  object BodyTypeCRUD extends AbstractCRUD[BodyType, BodyTypeTable] {
    lazy val dataTable: TableQuery[BodyTypeTable] = TableQuery[BodyTypeTable]
  }
}
