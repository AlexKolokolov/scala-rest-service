package org.kolokolov.repo

import org.kolokolov.model.Order
import org.kolokolov.model.OrderStatus
import org.kolokolov.model.OrderStatus.OrderStatus
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Kolokolov on 16.05.2017.
  */
trait OrderCRUDModule extends CustomerCRUDModule {

  self: DatabaseProfile =>

  import profile.api._

  class OrderTable(tag: Tag) extends Table[Order](tag, "order") with IdentifiableTable[Order] {
    implicit val orderStatusMapper = MappedColumnType.base[OrderStatus,String](
      e => e.toString,
      s => OrderStatus.withName(s)
    )
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def customerId = column[Int]("customer_id")
    def status = column[OrderStatus]("status")
    def customer = foreignKey("order_cust_fk", customerId, CustomerCRUD.dataTable)(_.id,
      onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
    def * = (customerId, status, id) <> (Order.tupled, Order.unapply)
  }


  object OrderCRUD extends AbstractCRUD[Order, OrderTable] {
    lazy val dataTable: TableQuery[OrderTable] = TableQuery[OrderTable]

    def getOrdersByCustomerId(customerId: Int): Future[Seq[Order]] = {
      val getOrdersByCustomerIdAction = dataTable.filter(_.customerId === customerId).result
      database.run(getOrdersByCustomerIdAction)
    }

    def getCustomersOrderById(orderId: Int, customerId: Int): Future[Option[Order]] = {
      val getOrdersByCustomerIdAction = dataTable.filter(_.id === orderId).filter(_.customerId === customerId).result.headOption
      database.run(getOrdersByCustomerIdAction)
    }

    override def update(order: Order)(implicit ec: ExecutionContext): Future[Int] = {
      val updateOrderStatusAction = dataTable.filter(_.id === order.id).
        filter(_.customerId === order.customerId).update(order)
      database.run(updateOrderStatusAction)
    }
  }
}