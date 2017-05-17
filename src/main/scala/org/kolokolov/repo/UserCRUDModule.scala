package org.kolokolov.repo

import org.kolokolov.model.User

/**
  * Created by Kolokolov on 16.05.2017.
  */
trait UserCRUDModule extends AbstractCRUDModule {

  self: DatabaseProfile =>

  import profile.api._

  class UserTable(tag: Tag) extends Table[User](tag, "user_table") with IdentifiableTable[User] {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (name, id) <> (User.tupled, User.unapply)
  }

  object UserCRUD extends AbstractCRUD[User, UserTable] {
    lazy val dataTable: TableQuery[UserTable] = TableQuery[UserTable]
  }
}
