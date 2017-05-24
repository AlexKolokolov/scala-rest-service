package org.kolokolov.repo

import org.kolokolov.model.Product

/**
  * Created by Kolokolov on 16.05.2017.
  */
trait ProductCRUDModule extends ProductVendorCRUDModule with ProductCategoryCRUDModule {

  self: DatabaseProfile =>

  import profile.api._

  class ProductTable(tag: Tag) extends Table[Product](tag, "product") with IdentifiableTable[Product] {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def categoryId = column[Int]("categoryId")
    def vendorId = column[Int]("vendorId")
    def category = foreignKey("prod_cat_fk", vendorId, ProductCategoryCRUD.dataTable)(_.id,
      onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
    def vendor = foreignKey("prod_vend_fk", vendorId, ProductVendorCRUD.dataTable)(_.id,
      onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
    def * = (name, categoryId, vendorId, id) <> (Product.tupled, Product.unapply)
  }

  object ProductCRUD extends AbstractCRUD[Product, ProductTable] {
    lazy val dataTable: TableQuery[ProductTable] = TableQuery[ProductTable]
  }
}
