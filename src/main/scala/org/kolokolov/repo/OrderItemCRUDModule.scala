package org.kolokolov.repo

import org.kolokolov.model.{Order, OrderItem}

import scala.concurrent.Future

/**
  * Created by Kolokolov on 16.05.2017.
  */
trait OrderItemCRUDModule extends ProductCRUDModule with OrderCRUDModule {

  self: DatabaseProfile =>

  import profile.api._

  class OrderItemTable(tag: Tag) extends Table[OrderItem](tag, "order_item") with IdentifiableTable[OrderItem] {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def orderId = column[Int]("orderId")
    def productId = column[Int]("productId")
    def quantity = column[Int]("quantity")
    def order = foreignKey("item_order_fk", orderId, OrderCRUD.dataTable)(_.id,
      onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
    def product = foreignKey("item_prod_fk", productId, ProductCRUD.dataTable)(_.id,
      onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
    def * = (orderId, productId, quantity, id) <> (OrderItem.tupled, OrderItem.unapply)
  }

  object OrderItemCRUD extends AbstractCRUD[OrderItem, OrderItemTable] {
    lazy val dataTable: TableQuery[OrderItemTable] = TableQuery[OrderItemTable]

    def getItemByOrderId(orderId: Int): Future[Seq[OrderItem]] = {
      val getItemByOrderIdAction = dataTable.filter(_.orderId === orderId).result
      database.run(getItemByOrderIdAction)
    }

    def getAllItemsOfCustomersOrderById(orderId: Int, customerId: Int): Future[Seq[OrderItem]] = {
      val getItemsOfCustomersOrderByIdAction = {
        for {
          o <- OrderCRUD.dataTable if o.id === orderId && o.customerId === customerId
          i <- dataTable if i.orderId === o.id
        } yield i
      }.result
      database.run(getItemsOfCustomersOrderByIdAction)
    }

    def deleteItemFromOrderById(itemId: Int, orderId: Int): Future[Int] = {
      val deleteItemFromOrderByIdAction = dataTable.filter(_.id === itemId).filter(_.orderId === orderId).delete
      database.run(deleteItemFromOrderByIdAction)
    }

    override def update(item: OrderItem): Future[Int] = {
      val updateProductQuantityAction = dataTable.filter(_.id === item.id).
        filter(_.productId === item.productId).update(item)
      database.run(updateProductQuantityAction)
    }
  }
}
