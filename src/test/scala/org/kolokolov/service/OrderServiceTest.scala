package org.kolokolov.service

import org.kolokolov.model.{Order, OrderItem, OrderStatus}
import org.kolokolov.repo.H2Database
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * Created by Kolokolov on 5/10/17.
  */
class OrderServiceTest extends AsyncFunSuite
  with Matchers
  with BeforeAndAfterEach {

  private val orderService = new OrderService(H2Profile)

  import orderService._

  private val dbTestHelper = new TestDBCreator with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  test("getAllOrders should return Seq(Order(1,OrderStatus.Created,1), Order(2,OrderStatus.Created,2))") {
    getAllOrders.map { result =>
      result shouldEqual Seq(Order(1,OrderStatus.Created,1), Order(2,OrderStatus.Created,2))
    }
  }

  test("getOrdersByCustomerId(1) should return Seq(Order(1,OrderStatus.Created,1))") {
    getOrdersByCustomerId(1).map { result =>
      result shouldEqual Seq(Order(1,OrderStatus.Created,1))
    }
  }

  test("getOrdersByCustomerId(3) should return empty Seq") {
    getOrdersByCustomerId(3).map { result =>
      result shouldEqual Seq.empty
    }
  }

  test("getCustomersOrderById(1,1) should return Some(Order(1,OrderStatus.Created,1))") {
    getCustomersOrderById(1,1).map { result =>
      result shouldEqual Some(Order(1,OrderStatus.Created,1))
    }
  }

  test("getCustomersOrderById(1,2) should return None") {
    getCustomersOrderById(1,2).map { result =>
      result shouldEqual None
    }
  }

  test("getOrderById(1) should return Some(Order(1,OrderStatus.Created,1))") {
    getOrderById(1).map { result =>
      result shouldEqual Some(Order(1,OrderStatus.Created,1))
    }
  }

  test("getOrderById(3) should return None") {
    getOrderById(3).map { result =>
      result shouldEqual None
    }
  }

  test("getOrderById(1) should return None after deleteOrder(1)") {
    deleteOrder(1).flatMap { delResult =>
        delResult shouldEqual 1
        getOrderById(1).map { result =>
          result shouldEqual None
      }
    }
  }

  test("deleteOrder(3) should return 0") {
    deleteOrder(3).map { result =>
      result shouldEqual 0
    }
  }

  test("getOrderById(3) should return Some(Order(1,OrderStatus.Created,3) after createNewOrder(Order(1))") {
    createNewOrder(Order(1)).flatMap { saveResult =>
      saveResult shouldEqual 3
      getOrderById(3).map { result =>
        result shouldEqual Some(Order(1,OrderStatus.Created,3))
      }
    }
  }

  test("createNewOrder(Order(3)) should return -1") {
    createNewOrder(Order(3)).map { saveResult =>
      saveResult shouldEqual -1
    }
  }

  test("getOrderById(1) should return Some(Order(1,OrderStatus.Confirmed,1))) after updateOrderStatus(Order(1,OrderStatus.Confirmed,1))") {
    updateOrderStatus(Order(1,OrderStatus.Confirmed,1)).flatMap { updateResult =>
      updateResult shouldEqual 1
      getOrderById(1).map { result =>
        result shouldEqual Some(Order(1,OrderStatus.Confirmed,1))
      }
    }
  }

  test("updateOrderStatus(Order(1,OrderStatus.Created,3)) should return 0") {
    updateOrderStatus(Order(1,OrderStatus.Created,3)).map { result =>
      result shouldEqual 0
    }
  }

  test("updateOrderStatus(Order(2,OrderStatus.Created,1)) should return 0") {
    updateOrderStatus(Order(2,OrderStatus.Created,1)).map { result =>
      result shouldEqual 0
    }
  }

  test("updateOrderStatus(Order(3,OrderStatus.Created,1)) should return 0") {
    updateOrderStatus(Order(3,OrderStatus.Created,1)).map { result =>
      result shouldEqual 0
    }
  }

  test("getItemsByOrderId(1) should return Seq(OrderItem(1,1,5,1),OrderItem(1,3,5,2))") {
    getItemsByOrderId(1).map { result =>
      result shouldEqual Seq(OrderItem(1,1,5,1),OrderItem(1,3,5,2))
    }
  }

  test("getItemsByOrderId(3) should return empty Seq") {
    getItemsByOrderId(3).map { result =>
      result shouldEqual Seq.empty
    }
  }

  test("getItemsOfCustomerOrder(1,1) should return Seq(OrderItem(1,1,5,1),OrderItem(1,3,5,2))") {
    getItemsOfCustomerOrder(orderId = 1,customerId = 1).map { result =>
      result shouldEqual Seq(OrderItem(1,1,5,1),OrderItem(1,3,5,2))
    }
  }

  test("getItemsOfCustomerOrder(1,2) should return empty Seq") {
    getItemsOfCustomerOrder(orderId = 1,customerId = 2).map { result =>
      result shouldEqual Seq.empty
    }
  }

  test("getItemsByOrderId(1) should return Seq with length 3 after addNewItem(OrderItem(1,2,3))") {
    addNewItem(OrderItem(orderId = 1,productId = 2,quantity = 3)).flatMap { addResult =>
      addResult shouldEqual 5
      getItemsByOrderId(1).map { result =>
        result.length shouldEqual 3
      }
    }
  }

  test("addNewItem(OrderItem(3,2,3))") {
    addNewItem(OrderItem(orderId = 3,productId = 2,quantity = 3)).map { addResult =>
      addResult shouldEqual -1
    }
  }

  test("addNewItem(OrderItem(1,5,3))") {
    addNewItem(OrderItem(orderId = 1,productId = 5,quantity = 3)).map { addResult =>
      addResult shouldEqual -1
    }
  }

  test("getItemsByOrderId(1) should return Seq(OrderItem(1,1,10,1),OrderItem(1,3,5,2)) after updateProductQuantity(OrderItem(1,1,10,1))") {
    updateProductQuantityInItem(OrderItem(orderId = 1,productId = 1,quantity = 10,id = 1)).flatMap { updateResult =>
      updateResult shouldEqual 1
      getItemsByOrderId(1).map { result =>
        result shouldEqual Seq(OrderItem(1,1,10,1),OrderItem(1,3,5,2))
      }
    }
  }

  test("updateProductQuantity(OrderItem(1,2,10,1)) should return 0") {
    updateProductQuantityInItem(OrderItem(orderId = 1,productId = 2,quantity = 10,id = 1)).map { updateResult =>
      updateResult shouldEqual 0
    }
  }

  test("updateProductQuantity(OrderItem(2,1,10,1)) should return 0") {
    updateProductQuantityInItem(OrderItem(orderId = 2,productId = 1,quantity = 10,id = 1)).map { updateResult =>
      updateResult shouldEqual 0
    }
  }

  test("getItemsByOrderId(1) should return Seq(OrderItem(1,3,5,2)) after removeItemFromOrder(1,1)") {
    removeItemFromOrder(itemId = 1, orderId = 1).flatMap { deleteResult =>
      deleteResult shouldEqual 1
      getItemsByOrderId(1).map { result =>
        result shouldEqual Seq(OrderItem(1,3,5,2))
      }
    }
  }

  test("removeItemFromOrder(1,2) should return 0") {
    removeItemFromOrder(itemId = 1, orderId = 2).flatMap { deleteResult =>
      deleteResult shouldEqual 0
    }
  }
}