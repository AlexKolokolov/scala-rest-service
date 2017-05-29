package org.kolokolov.rest.integration

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.kolokolov.repo.H2Database
import org.kolokolov.rest.{JsonSupport, SwaggerShopRestController}
import org.kolokolov.service.TestDBCreator
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import akka.http.scaladsl.model._
import org.kolokolov.model._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Kolokolov on 11.05.2017.
  */
class RestControllerTest extends AsyncFunSuite
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfterEach
  with JsonSupport{

  private val routes = (new SwaggerShopRestController(ActorSystem("test-actor-system")) with H2Database).routes

  val dbTestHelper = new TestDBCreator with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  // /customers rout tests
  test("GET /customers should return all customers") {
    Get("/customers") ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[Customer]] shouldEqual Seq(Customer("Bob Marley",1), Customer("Tom Waits",2))
    }
  }

  test("GET /customers/1 should return Customer(Bob Marley,1)") {
    Get("/customers/1") ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Customer] shouldEqual Customer("Bob Marley",1)
    }
  }

  test("GET /customers/3 should return 'Customer with ID: 3 was not found'") {
    Get("/customers/3") ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Customer with ID: 3 was not found"
    }
  }

  test("POST /customers with Customer(Marlon Brando) should return Customer(Marlon Brando,3)") {
    Post("/customers",Customer("Marlon Brando")) ~> routes ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Customer] shouldEqual Customer("Marlon Brando",3)
    }
  }

  test("DELETE /customers/1 status should be 205 ResetContent") {
    Delete("/customers/1") ~> routes ~> check {
      status shouldEqual StatusCodes.ResetContent
    }
  }

  test("DELETE /customers/3 should return 'Customer with ID: 3 was not found'") {
    Delete("/customers/3") ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Customer with ID: 3 was not found"
    }
  }

  test("PUT /customers with Customer(Marlon Brando, 1) status should be 205 ResetContent") {
    Put("/customers",Customer("Marlon Brando",1)) ~> routes ~> check {
      status shouldEqual StatusCodes.ResetContent
    }
  }

  test("PUT /customers with Customer(Marlon Brando, 3) should return 'Customer with ID: 3 was not found'") {
    Put("/customers",Customer("Marlon Brando",3)) ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Customer with ID: 3 was not found"
    }
  }

  test("GET /customers/1/orders should return Seq(Order(1,OrderStatus.Created,1)") {
    Get("/customers/1/orders") ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[Order]] shouldEqual Seq(Order(1,OrderStatus.Created,1))
    }
  }

  test("GET /customers/3/orders should return empty Seq") {
    Get("/customers/3/orders") ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[Order]] shouldEqual Seq.empty
    }
  }

  test("GET /customers/1/orders/1 should return Order(1,OrderStatus.Created,1)") {
    Get("/customers/1/orders/1") ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Order] shouldEqual Order(1,OrderStatus.Created,1)
    }
  }

  test("GET /customers/1/orders/2 should return 'Order with ID: 2 of customer with ID: 1 was not found'") {
    Get("/customers/1/orders/2") ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Order with ID: 2 of customer with ID: 1 was not found"
    }
  }

  test("GET /customers/1/orders/1/items should return Seq(OrderItem(1,1,5),OrderItem(1,3,5))") {
    Get("/customers/1/orders/1/items") ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[OrderItem]] shouldEqual Seq(OrderItem(1,1,5,1),OrderItem(1,3,5,2))
    }
  }

  test("GET /customers/1/orders/2/items should return empty Seq") {
    Get("/customers/1/orders/2/items") ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[OrderItem]] shouldEqual Seq.empty
    }
  }

  // /categories route tests

  test("GET /categories should return all categories") {
    Get("/categories") ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[ProductCategory]] shouldEqual Seq(ProductCategory("SMG",1), ProductCategory("Sidearm",2))
    }
  }

  test("GET /categories/1 should return ProductCategory(SMG,1)") {
    Get("/categories/1") ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[ProductCategory] shouldEqual ProductCategory("SMG",1)
    }
  }

  test("GET /categories/3 should return 'Category with ID: 3 was not found'") {
    Get("/categories/3") ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Category with ID: 3 was not found"
    }
  }

  test("POST /categories with ProductCategory(Rifle) should return ProductCategory(Rifle,3))") {
    Post("/categories",ProductCategory("Rifle")) ~> routes ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[ProductCategory] shouldEqual ProductCategory("Rifle",3)
    }
  }

  test("DELETE /categories/1 status should be 205 ResetContent") {
    Delete("/categories/1") ~> routes ~> check {
      status shouldEqual StatusCodes.ResetContent
    }
  }

  test("DELETE /categories/3 should return 'Category with ID: 3 was not found'") {
    Delete("/categories/3") ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Category with ID: 3 was not found"
    }
  }

  test("PUT /categories with ProductCategory(Rifle, 1) status should be 205 ResetContent") {
    Put("/categories",ProductCategory("Rifle",1)) ~> routes ~> check {
      status shouldEqual StatusCodes.ResetContent
    }
  }

  test("PUT /categories with ProductCategory(Rifle,3) should return 'Category with ID: 3 was not found'") {
    Put("/categories",ProductCategory("Rifle",3)) ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Category with ID: 3 was not found"
    }
  }

  // /vendors route tests

  test("GET /vendors should return all vendors") {
    Get("/vendors") ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[ProductVendor]] shouldEqual Seq(ProductVendor("H&K",1), ProductVendor("FN",2))
    }
  }

  test("GET /vendors/1 should return ProductVendor(H&K,1)") {
    Get("/vendors/1") ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[ProductVendor] shouldEqual ProductVendor("H&K",1)
    }
  }

  test("GET /vendors/3 should return 'Vendor with ID: 3 was not found'") {
    Get("/vendors/3") ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Vendor with ID: 3 was not found"
    }
  }

  test("POST /vendors with ProductVendor(IMI) should return ProductCategory(IMI,3))") {
    Post("/vendors",ProductVendor("IMI")) ~> routes ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[ProductVendor] shouldEqual ProductVendor("IMI",3)
    }
  }

  test("DELETE /vendors/1 status should be 205 ResetContent") {
    Delete("/vendors/1") ~> routes ~> check {
      status shouldEqual StatusCodes.ResetContent
    }
  }

  test("DELETE /vendors/3 should return 'Vendor with ID: 3 was not found'") {
    Delete("/vendors/3") ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Vendor with ID: 3 was not found"
    }
  }

  test("PUT /vendors with ProductVendor(IMI, 1) status should be 205 ResetContent") {
    Put("/vendors",ProductVendor("IMI",1)) ~> routes ~> check {
      status shouldEqual StatusCodes.ResetContent
    }
  }

  test("PUT /vendors with ProductVendor(IMI,3) should return 'Vendor with ID: 3 was not found'") {
    Put("/vendors",ProductVendor("IMI",3)) ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Vendor with ID: 3 was not found"
    }
  }

  // /products route tests

  test("GET /products should return all products") {
    Get("/products") ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[Product]] shouldEqual Seq(Product("MP5",1,1,1),Product("UPS",2,1,2),Product("P90",1,2,3),Product("FiveSeven",2,2,4))
    }
  }

  test("GET /products/1 should return Product(MP5,1,1)") {
    Get("/products/1") ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Product] shouldEqual Product("MP5",1,1,1)
    }
  }

  test("GET /products/5 should return 'Product with ID: 5 was not found'") {
    Get("/products/5") ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Product with ID: 5 was not found"
    }
  }

  test("POST /products with Product(UMP,1,1) should return Product(UMP,1,1,5))") {
    Post("/products",Product("UMP",categoryId = 1,vendorId = 1)) ~> routes ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Product] shouldEqual Product("UMP",1,1,5)
    }
  }

  test("POST /products with Product(UMP,3,1) should return 'Product with category ID: 3 and vendor ID: 1 cannot be added'") {
    Post("/products",Product("UMP",categoryId = 3,vendorId = 1)) ~> routes ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[String] shouldEqual "Product with category ID: 3 and vendor ID: 1 cannot be added"
    }
  }

  test("POST /products with Product(UMP,1,3) should return 'Product with category ID: 1 and vendor ID: 3 cannot be added'") {
    Post("/products",Product("UMP",categoryId = 1,vendorId = 3)) ~> routes ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[String] shouldEqual "Product with category ID: 1 and vendor ID: 3 cannot be added"
    }
  }

  test("DELETE /products/1 status should be 205 ResetContent") {
    Delete("/products/1") ~> routes ~> check {
      status shouldEqual StatusCodes.ResetContent
    }
  }

  test("DELETE /products/5 should return 'Product with ID: 5 was not found'") {
    Delete("/products/5") ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Product with ID: 5 was not found"
    }
  }

  test("PUT /products with Product(UMP,1,1,1) status should be 205 ResetContent") {
    Put("/products",Product("UMP",1,1,1)) ~> routes ~> check {
      status shouldEqual StatusCodes.ResetContent
    }
  }

  test("PUT /products with Product(UMP,1,1,5)) should return 'Product with ID: 5 was not found'") {
    Put("/products",Product("UMP",1,1,5)) ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Product with ID: 5 was not found"
    }
  }

  test("PUT /products with Product(UMP,3,1,1)) should return 'Product has illegal category ID: 3 or vendor ID: 1'") {
    Put("/products",Product("UMP",3,1,1)) ~> routes ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[String] shouldEqual "Product has illegal category ID: 3 or vendor ID: 1"
    }
  }

  test("PUT /products with Product(UMP,1,3,1)) should return 'Product has illegal category ID: 1 or vendor ID: 3'") {
    Put("/products",Product("UMP",1,3,1)) ~> routes ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[String] shouldEqual "Product has illegal category ID: 1 or vendor ID: 3"
    }
  }
}