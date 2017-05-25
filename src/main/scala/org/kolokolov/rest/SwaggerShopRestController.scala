package org.kolokolov.rest

import javax.ws.rs.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.swagger.annotations._
import org.kolokolov.model._
import org.kolokolov.repo.DatabaseProfile
import org.kolokolov.service._
import org.scalatest.path
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by Kolokolov on 11.05.2017.
  */
@Api(value = "/webapi", produces = "application/json")
@Path("/")
class SwaggerShopRestController(system: ActorSystem) extends JsonSupport {

  this: DatabaseProfile =>

  lazy val customerService = new CustomerService(profile)
  lazy val productService = new ProductService(profile)
  lazy val productCategoryService = new ProductCategoryService(profile)
  lazy val productVendorService = new ProductVendorService(profile)
  lazy val orderService = new OrderService(profile)

  implicit val executionContext = system.dispatcher

  private val logger = LoggerFactory.getLogger(this.getClass)

  val routes = Route {
    getAllCustomers ~ getCustomerById ~ getAllOrdersOfCustomer ~ getOrderOfCustomer ~ saveNewCustomer
  }

  @ApiOperation(value = "Returns All Customers", notes = "", nickname = "getAllCustomers", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Returns All Customers", response = classOf[Customer], responseContainer = "List"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/customers")
  def getAllCustomers = {
    get{
      pathPrefix("customers") {
        pathEndOrSingleSlash {
          val customersFuture = customerService.getAllCustomers
          onComplete(customersFuture) {
            case Success(customers) => complete(customers)
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Saves New Customer", notes = "", nickname = "saveNewCustomer", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "customer", value = "New Customer", required = true, dataType = "org.kolokolov.model.Customer", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "New Customer Has Been Saved"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/customers")
  def saveNewCustomer = {
    post{
      pathPrefix("customers") {
        pathEndOrSingleSlash {
          entity(as[Customer]) { customer =>
            val savedCustomer = customerService.addNewCustomer(customer)
            onComplete(savedCustomer) {
              case Success(1) => complete(StatusCodes.Created, s"New customer $customer has been added")
              case Success(_) => complete(StatusCodes.BadRequest)
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Returns Customer By ID", notes = "", nickname = "getCustomerById", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "customerId", value = "Customer ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return Customer By ID", response = classOf[Customer]),
    new ApiResponse(code = 404, message = "Customer Not Found", response = classOf[String]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/customers/{customerId}")
  def getCustomerById = {
    get{
      pathPrefix("customers" / IntNumber) { customerId =>
        pathEndOrSingleSlash {
          val customerFuture = customerService.getCustomerById(customerId)
          onComplete(customerFuture) {
            case Success(Some(customer)) => complete(customer)
            case Success(None) => complete(StatusCodes.NotFound, s"Customer with ID: $customerId was not found")
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Returns All Orders Of Customer With ID", notes = "", nickname = "getAllOrdersOfCustomer", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "customerId", value = "Customer ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return All Orders Of Customer With ID", response = classOf[Order], responseContainer = "List"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/customers/{customerId}/orders")
  def getAllOrdersOfCustomer = {
    get{
      pathPrefix("customers" / IntNumber / "orders") { customerId =>
        pathEndOrSingleSlash {
          val ordersFuture = orderService.getOrdersByCustomerId(customerId)
          onComplete(ordersFuture) {
            case Success(orders) => complete(orders)
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Returns Order With ID Of Customer With ID", notes = "", nickname = "getOrderOfCustomer", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "customerId", value = "Customer ID", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "orderId", value = "Customer ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return Order With ID Of Customer With ID", response = classOf[Order]),
    new ApiResponse(code = 404, message = "Order Not Found", response = classOf[String]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/customers/{customerId}/orders/{orderId}")
  def getOrderOfCustomer = {
    get{
      pathPrefix("customers" / IntNumber / "orders") { customerId =>
        pathPrefix(IntNumber) { orderId =>
          pathEndOrSingleSlash {
            val orderFuture = orderService.getCustomersOrderById(orderId, customerId)
            onComplete(orderFuture) {
              case Success(Some(order)) => complete(order)
              case Success(None) => complete(StatusCodes.NotFound, s"Order with ID: $orderId by user with ID: $customerId not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      }
    }
  }


}


