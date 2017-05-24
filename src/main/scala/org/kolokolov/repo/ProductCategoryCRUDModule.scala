package org.kolokolov.repo

import org.kolokolov.model.ProductCategory

/**
  * Created by Kolokolov on 16.05.2017.
  */
trait ProductCategoryCRUDModule extends AbstractCRUDModule {

  self: DatabaseProfile =>

  import profile.api._

  class ProductCategoryTable(tag: Tag) extends Table[ProductCategory](tag, "product_category") with IdentifiableTable[ProductCategory] {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def * = (title, id) <> (ProductCategory.tupled, ProductCategory.unapply)
  }

  object ProductCategoryCRUD extends AbstractCRUD[ProductCategory, ProductCategoryTable] {
    lazy val dataTable: TableQuery[ProductCategoryTable] = TableQuery[ProductCategoryTable]
  }
}
