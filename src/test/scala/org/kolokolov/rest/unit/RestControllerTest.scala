package org.kolokolov.rest.unit

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.kolokolov.model._
import org.kolokolov.repo.H2Database
import org.kolokolov.rest.{JsonSupport, RestController}
import org.kolokolov.service.{CustomerService, OrderService, ProductCategoryService}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.Future

/**
  * Created by Kolokolov on 11.05.2017.
  */
class RestControllerTest extends AsyncFunSuite
  with AsyncMockFactory
  with Matchers
  with ScalatestRouteTest
  with JsonSupport {

  private val customer1 = Customer("Bob Marley",1)
  private val customer2 = Customer("Tam Waits",2)

  private val category1 = ProductCategory("SMG",1)
  private val category2 = ProductCategory("Sidearm",2)

  private val vendor1 = ProductVendor("H&K",1)
  private val vendor2 = ProductVendor("FN",2)

  private val product1 = Product("MP5",categoryId = 1,vendorId = 1,id = 1)
  private val product2 = Product("P90",categoryId = 1,vendorId = 2,id = 2)

  private val order1 = Order(customerId = 1,status = OrderStatus.Created,id = 1)
  private val order2 = Order(customerId = 1,status = OrderStatus.Created,id = 2)


  def createCustomersRoute: Route = {

    val stubCustomerService = stub[CustomerService]
    (stubCustomerService.getCustomerById _).when(1).returns(Future(Some(customer1)))
    (stubCustomerService.getAllCustomers _).when().returns(Future(Seq(customer1,customer2)))

    val stubOrderService = stub[OrderService]
    (stubOrderService.getCustomersOrderById _).when(1,1).returns(Future(Some(order1)))
    (stubOrderService.getOrdersByCustomerId _).when(1).returns(Future(Seq(order1,order2)))

    val controller = new RestController(system) with H2Database {
      override lazy val customerService = stubCustomerService
      override lazy val orderService = stubOrderService
    }
    controller.routes
  }

  def createCategoriesRoute: Route = {

    val stubCategoryService = stub[ProductCategoryService]
    (stubCategoryService.getAllCategories _).when().returns(Future(Seq(category1,category2)))
    (stubCategoryService.getCategoryById _).when(1).returns(Future(Some(category1)))

    val controller = new RestController(system) with H2Database {
      override lazy val productCategoryService = stubCategoryService
    }
    controller.routes
  }

  test("GET /customers should return all customers") {
    val route = createCustomersRoute
    Get("/customers") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[Customer]] shouldEqual Seq(customer1,customer2)
    }
  }

  test("GET /customers/1 should return customer1") {
    val route = createCustomersRoute
    Get("/customers/1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Customer] shouldEqual customer1
    }
  }

  test("GET /customers/1/orders should return Seq(order1,order2)") {
    val route = createCustomersRoute
    Get("/customers/1/orders") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[Order]] shouldEqual Seq(order1,order2)
    }
  }

  test("GET /customers/1/orders/1 should return order1") {
    val route = createCustomersRoute
    Get("/customers/1/orders/1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Order] shouldEqual order1
    }
  }

  test("GET /categories should return all categories") {
    val route = createCategoriesRoute
    Get("/categories") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[ProductCategory]] shouldEqual Seq(category1,category2)
    }
  }

  test("GET /categories/1 should return category1") {
    val route = createCategoriesRoute
    Get("/categories/1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[ProductCategory] shouldEqual category1
    }
  }

}