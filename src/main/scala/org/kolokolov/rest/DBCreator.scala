package org.kolokolov.rest

import org.kolokolov.model._
import org.kolokolov.repo._

import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class DBCreator extends CustomerCRUDModule
  with ProductCategoryCRUDModule
  with ProductVendorCRUDModule
  with ProductCRUDModule
  with OrderItemCRUDModule
  with OrderCRUDModule {

  this: DatabaseProfile =>

  import profile.api._

  def setupDB: Future[Unit] = {
    val setup = DBIO.seq(
      CustomerCRUD.dataTable.schema.create,
      ProductVendorCRUD.dataTable.schema.create,
      ProductCategoryCRUD.dataTable.schema.create,
      ProductCRUD.dataTable.schema.create,
      OrderCRUD.dataTable.schema.create,
      OrderItemCRUD.dataTable.schema.create,
      CustomerCRUD.dataTable ++= Seq(Customer("Bob Marley"), Customer("Tom Waits")),
      ProductVendorCRUD.dataTable ++= Seq(ProductVendor("H&K"), ProductVendor("FN")),
      ProductCategoryCRUD.dataTable ++= Seq(ProductCategory("SMG"), ProductCategory("Sidearm")),
      ProductCRUD.dataTable ++= Seq(Product("MP5",1,1),Product("UPS",2,1),Product("P90",1,2),Product("FiveSeven",2,2)),
      OrderCRUD.dataTable ++= Seq(Order(1,OrderStatus.Created), Order(2,OrderStatus.Created)),
      OrderItemCRUD.dataTable ++= Seq(OrderItem(1,1,5),OrderItem(1,3,5),OrderItem(2,2,1),OrderItem(2,4,1))
    ).transactionally
    database.run(setup)
  }

  def cleanDB: Future[Unit] = {
    val dropTables = DBIO.seq(
      OrderItemCRUD.dataTable.schema.drop,
      OrderCRUD.dataTable.schema.drop,
      ProductCRUD.dataTable.schema.drop,
      ProductVendorCRUD.dataTable.schema.drop,
      ProductCategoryCRUD.dataTable.schema.drop,
      CustomerCRUD.dataTable.schema.drop
    ).transactionally
    database.run(dropTables)
  }
}