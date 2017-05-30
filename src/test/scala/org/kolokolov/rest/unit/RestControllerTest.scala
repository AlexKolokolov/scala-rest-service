package org.kolokolov.rest.unit

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.kolokolov.model._
import org.kolokolov.repo.H2Database
import org.kolokolov.rest.{JsonSupport,SwaggerShopRestController}
import org.kolokolov.service.CustomerService
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

  private val firstCustomer = Customer("Bob Marley",1)
  private val secondCustomer = Customer("Tam Waits",2)

  private val firstCategory = ProductCategory("SMG",1)
  private val secondCategory = ProductCategory("Sidearm",2)

  private val firstVendor = ProductVendor("H&K",1)
  private val secondVendor = ProductVendor("FN",2)

  private val firstProduct = Product("MP5",categoryId = 1,vendorId = 1,id = 1)
  private val secondProduct = Product("P90",categoryId = 1,vendorId = 2,id = 2)

  def createRoute: Route = {
    val stubCustomerService = stub[CustomerService]
    (stubCustomerService.getCustomerById _).when(1).returns(Future(Some(firstCustomer)))
    (stubCustomerService.getAllCustomers _).when().returns(Future(Seq(firstCustomer,secondCustomer)))
    val controller = new SwaggerShopRestController(system) with H2Database {
      override lazy val customerService = stubCustomerService
    }
    controller.routes
  }

  test("GET /customers should return all customers") {
    val route = createRoute
    Get("/customers") ~> route ~> check {
      responseAs[Seq[Customer]] shouldEqual Seq(firstCustomer,secondCustomer)
    }
  }

  test("GET /customers/1 should return firstCustomer") {
    val route = createRoute
    Get("/customers/1") ~> route ~> check {
      responseAs[Customer] shouldEqual firstCustomer
    }
  }

}