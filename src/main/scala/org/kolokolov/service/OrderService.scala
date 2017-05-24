package org.kolokolov.service

import org.kolokolov.model.{Order, OrderItem}
import org.kolokolov.repo.{DatabaseProfile, OrderCRUDModule, OrderItemCRUDModule}
import slick.jdbc.JdbcProfile
import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class OrderService(override val profile: JdbcProfile) extends OrderCRUDModule with OrderItemCRUDModule with DatabaseProfile {

  def getAllOrders: Future[Seq[Order]] = OrderCRUD.getAll
  def getOrderById(id: Int): Future[Option[Order]] = OrderCRUD.getById(id)
  def getOrdersByCustomerId(customerId: Int): Future[Seq[Order]] = OrderCRUD.getOrdersByCustomerId(customerId)
  def getCustomersOrderById(orderId:Int, customerId: Int): Future[Option[Order]] = OrderCRUD.getCustomersOrderById(orderId,customerId)
  def createNewOrder(order: Order): Future[Int] = OrderCRUD.save(order)
  def deleteOrder(id: Int): Future[Int] = OrderCRUD.delete(id)
  def updateOrderStatus(orders: Order): Future[Int] = OrderCRUD.update(orders)
  def getItemsByOrderId(id: Int): Future[Seq[OrderItem]] = OrderItemCRUD.getItemByOrderId(id)
  def getAllItemsOfCustomerOrderById(orderId: Int, customerId: Int): Future[Seq[OrderItem]] = OrderItemCRUD.getAllItemsOfCustomersOrderById(orderId,customerId)
  def addNewItem(item: OrderItem): Future[Int] = OrderItemCRUD.save(item)
  def updateProductQuantity(item: OrderItem): Future[Int] = OrderItemCRUD.update(item)
  def removeItem(id: Int): Future[Int] = OrderItemCRUD.delete(id)
}