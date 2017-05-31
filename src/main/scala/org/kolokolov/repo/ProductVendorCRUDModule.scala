package org.kolokolov.repo

import org.kolokolov.model.ProductVendor

/**
  * Created by Kolokolov on 16.05.2017.
  */
trait ProductVendorCRUDModule extends AbstractCRUDModule {

  self: DatabaseProfile =>

  import profile.api._

  class ProductVendorTable(tag: Tag) extends Table[ProductVendor](tag, "product_vendor") with IdentifiableTable[ProductVendor] {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def * = (title, id) <> (ProductVendor.tupled, ProductVendor.unapply)
  }

  object ProductVendorCRUD extends AbstractCRUD[ProductVendor, ProductVendorTable] {
    lazy val dataTable: TableQuery[ProductVendorTable] = TableQuery[ProductVendorTable]
  }
}
