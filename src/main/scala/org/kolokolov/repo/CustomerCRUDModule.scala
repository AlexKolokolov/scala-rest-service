package org.kolokolov.repo

import org.kolokolov.model.Customer

/**
  * Created by Kolokolov on 16.05.2017.
  */
trait CustomerCRUDModule extends AbstractCRUDModule {

  self: DatabaseProfile =>

  import profile.api._

  class CustomerTable(tag: Tag) extends Table[Customer](tag, "customer") with IdentifiableTable[Customer] {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (name, id) <> (Customer.tupled, Customer.unapply)
  }

  object CustomerCRUD extends AbstractCRUD[Customer, CustomerTable] {
    lazy val dataTable: TableQuery[CustomerTable] = TableQuery[CustomerTable]
  }
}
