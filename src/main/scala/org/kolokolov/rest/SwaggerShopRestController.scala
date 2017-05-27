package org.kolokolov.rest


import javax.ws.rs.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import io.swagger.annotations._
import org.kolokolov.model._
import org.kolokolov.repo.DatabaseProfile
import org.kolokolov.service._
import org.slf4j.LoggerFactory

/**
  * Created by Kolokolov on 11.05.2017.
  */
@Api(value = "/webapi", produces = "application/json")
@javax.ws.rs.Path("/")
class SwaggerShopRestController(system: ActorSystem) extends JsonSupport {

  this: DatabaseProfile =>

  private val logger = LoggerFactory.getLogger(this.getClass)

  implicit val executionContext = system.dispatcher

  lazy val customerService = new CustomerService(profile)
  lazy val productService = new ProductService(profile)
  lazy val productCategoryService = new ProductCategoryService(profile)
  lazy val productVendorService = new ProductVendorService(profile)

  lazy val orderService = new OrderService(profile)

  val routes = Route {
    getAllCustomers ~ getCustomerById ~ saveNewCustomer ~ updateCustomer ~ deleteCustomerById ~
    getAllOrdersOfCustomer ~ getOrderOfCustomer ~ getAllItemsOfCustomerOrder ~
    getAllCategories ~ getCategoryById ~ addNewCategory ~ updateCategory ~ deleteCategoryById ~
    getAllVendors ~ getVendorById ~ addNewVendor ~ updateVendor ~ deleteVendorById ~
    getAllProducts ~ getProductById ~ addNewProduct ~ updateProduct ~ deleteProductById ~
    getAllOrders ~ getOrderById ~ createNewOrder ~ updateOrderStatus ~ deleteOrderById ~
    getAllItemsOfOrder ~ addItemToOrder ~ updateProductQuantityInItem ~ removeItemFromOrder
  }

  implicit def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case ex: Exception => {
        logger.error(ex.getStackTrace.mkString)
        complete(HttpResponse(StatusCodes.InternalServerError))
      }
    }

  @ApiOperation(value = "Returns All Customers", notes = "", nickname = "getAllCustomers", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "All customers", response = classOf[Customer], responseContainer = "List"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/customers")
  def getAllCustomers = {
    get{
      pathPrefix("customers") {
        pathEndOrSingleSlash {
          val customersRetrieval = customerService.getAllCustomers
          onSuccess(customersRetrieval) {  customers =>
            complete(customers)
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
    new ApiResponse(code = 201, message = "New customer has been saved"),
    new ApiResponse(code = 400, message = "Bad request"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/customers")
  def saveNewCustomer = {
    post {
      pathPrefix("customers") {
        pathEndOrSingleSlash {
          entity(as[Customer]) {
            case customer@Customer(name,_) =>
              val customerSaving = customerService.addNewCustomer(customer)
              onSuccess(customerSaving) { newId =>
                extractUri { uri =>
                  respondWithHeader(Location(uri + "/" + newId)) {
                    complete(StatusCodes.Created, Customer(name, newId))
                  }
                }
              }
            case _ => complete(StatusCodes.BadRequest)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates Customer", notes = "", nickname = "updateCustomer", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "customer", value = "Customer For Update", required = true, dataType = "org.kolokolov.model.Customer", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 205, message = "Customer has been updated"),
    new ApiResponse(code = 400, message = "Bad request"),
    new ApiResponse(code = 404, message = "Customer was nor found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/customers")
  def updateCustomer = {
    put {
      pathPrefix("customers") {
        pathEndOrSingleSlash {
          entity(as[Customer]) {
            case customer@Customer(_,id) =>
              val customersUpdating = customerService.updateCustomer(customer)
              onSuccess(customersUpdating) {
                case 1 => complete(StatusCodes.ResetContent)
                case _ => complete(StatusCodes.NotFound, s"Customer with ID: $id was not found")
              }
            case _ => complete(StatusCodes.BadRequest)
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
    new ApiResponse(code = 200, message = "Customer with ID", response = classOf[Customer]),
    new ApiResponse(code = 404, message = "Customer was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/customers/{customerId}")
  def getCustomerById = {
    get{
      pathPrefix("customers" / IntNumber) { customerId =>
        pathEndOrSingleSlash {
          val customerRetrieval = customerService.getCustomerById(customerId)
          onSuccess(customerRetrieval) {
            case Some(customer) => complete(customer)
            case None => complete(StatusCodes.NotFound, s"Customer with ID: $customerId was not found")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Deletes Customer By ID", notes = "", nickname = "deleteCustomerById", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "customerId", value = "Customer ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 205, message = "Customer has been deleted"),
    new ApiResponse(code = 404, message = "Customer was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/customers/{customerId}")
  def deleteCustomerById = {
    delete {
      pathPrefix("customers" / IntNumber) { customerId =>
        pathEndOrSingleSlash {
          val customerDeleting = customerService.deleteCustomer(customerId)
          onSuccess(customerDeleting) {
            case 1 => complete(StatusCodes.ResetContent)
            case _ => complete(StatusCodes.NotFound, s"Customer with ID: $customerId was not found")
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
    new ApiResponse(code = 200, message = "All orders of customer with ID", response = classOf[Order], responseContainer = "List"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/customers/{customerId}/orders")
  def getAllOrdersOfCustomer = {
    get{
      pathPrefix("customers" / IntNumber / "orders") { customerId =>
        pathEndOrSingleSlash {
          val ordersRetrieval = orderService.getOrdersByCustomerId(customerId)
          onSuccess(ordersRetrieval) { orders =>
            complete(orders)
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
    new ApiResponse(code = 200, message = "Order with ID of customer with ID", response = classOf[Order]),
    new ApiResponse(code = 404, message = "Order was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/customers/{customerId}/orders/{orderId}")
  def getOrderOfCustomer = {
    get{
      pathPrefix("customers" / IntNumber / "orders" / IntNumber) { (customerId, orderId) =>
        pathEndOrSingleSlash {
          val orderRetrieval = orderService.getCustomersOrderById(orderId, customerId)
          onSuccess(orderRetrieval) {
            case Some(order) => complete(order)
            case None => complete(StatusCodes.NotFound, s"Order with ID: $orderId of customer with ID: $customerId was not found")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Returns All Items Of Order With ID Of Customer With ID", notes = "", nickname = "getAllItemsOfCustomerOrder", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "customerId", value = "Customer ID", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "orderId", value = "Customer ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "All item of order with ID of customer with ID", response = classOf[OrderItem], responseContainer = "List"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/customers/{customerId}/orders/{orderId}/items")
  def getAllItemsOfCustomerOrder = {
    get{
      pathPrefix("customers" / IntNumber / "orders" / IntNumber / "items") { (customerId, orderId) =>
        pathEndOrSingleSlash {
          val itemsRetrieval = orderService.getAllItemsOfCustomerOrderById(orderId, customerId)
          onSuccess(itemsRetrieval) { items =>
            complete(items)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Returns All Product Categories", notes = "", nickname = "getAllCategories", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "All categories", response = classOf[ProductCategory], responseContainer = "List"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/categories")
  def getAllCategories = {
    get{
      pathPrefix("categories") {
        pathEndOrSingleSlash {
          val categoriesRetrieval = productCategoryService.getAllCategories
          onSuccess(categoriesRetrieval) {  categories =>
            complete(categories)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Adds New Product Category", notes = "", nickname = "addNewCategory", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "category", value = "New Product Category", required = true, dataType = "org.kolokolov.model.ProductCategory", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "New category has been added"),
    new ApiResponse(code = 400, message = "Bad request"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/categories")
  def addNewCategory = {
    post {
      pathPrefix("categories") {
        pathEndOrSingleSlash {
          entity(as[ProductCategory]) {
            case category@ProductCategory(title,_) =>
              val categoryAddition = productCategoryService.addNewCategory(category)
              onSuccess(categoryAddition) { newId =>
                extractUri { uri =>
                  respondWithHeader(Location(uri + "/" + newId)) {
                    complete(StatusCodes.Created, ProductCategory(title, newId))
                  }
                }
              }
            case _ => complete(StatusCodes.BadRequest)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates Product Category", notes = "", nickname = "updateCategory", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "category", value = "Category For Update", required = true, dataType = "org.kolokolov.model.ProductCategory", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 205, message = "Category has been updated"),
    new ApiResponse(code = 400, message = "Bad request"),
    new ApiResponse(code = 404, message = "Category was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/categories")
  def updateCategory = {
    put {
      pathPrefix("categories") {
        pathEndOrSingleSlash {
          entity(as[ProductCategory]) {
            case category@ProductCategory(_,id) =>
              val categoryUpdating = productCategoryService.updateCategory(category)
              onSuccess(categoryUpdating) {
                case 1 => complete(StatusCodes.ResetContent)
                case _ => complete(StatusCodes.NotFound, s"Category with ID: $id was not found")
              }
            case _ => complete(StatusCodes.BadRequest)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Returns Product Category By ID", notes = "", nickname = "getCategoryById", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "categoryId", value = "Category ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Category by ID", response = classOf[ProductCategory]),
    new ApiResponse(code = 404, message = "Category was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/categories/{categoryId}")
  def getCategoryById = {
    get{
      pathPrefix("categories" / IntNumber) { categoryId =>
        pathEndOrSingleSlash {
          val categoryRetrieval = productCategoryService.getCategoryById(categoryId)
          onSuccess(categoryRetrieval) {
            case Some(category) => complete(category)
            case None => complete(StatusCodes.NotFound, s"Category with ID: $categoryId was not found")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Deletes Category By ID", notes = "", nickname = "deleteCategoryById", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "categoryId", value = "Category ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 205, message = "Category has been deleted"),
    new ApiResponse(code = 404, message = "Category was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/categories/{categoryId}")
  def deleteCategoryById = {
    delete {
      pathPrefix("categories" / IntNumber) { categoryId =>
        pathEndOrSingleSlash {
          val categoryDeleting = productCategoryService.deleteCategory(categoryId)
          onSuccess(categoryDeleting) {
            case 1 => complete(StatusCodes.ResetContent)
            case _ => complete(StatusCodes.NotFound, s"Category with ID: $categoryId was not found")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Returns All Product Vendors", notes = "", nickname = "getAllVendors", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "All vendors", response = classOf[ProductVendor], responseContainer = "List"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/vendors")
  def getAllVendors = {
    get{
      pathPrefix("vendors") {
        pathEndOrSingleSlash {
          val vendorsRetrieval = productVendorService.getAllVendors
          onSuccess(vendorsRetrieval) {  vendors =>
            complete(vendors)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Adds New Product Vendor", notes = "", nickname = "addNewVendor", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "vendor", value = "New Product Vendor", required = true, dataType = "org.kolokolov.model.ProductVendor", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "New vendor has been added"),
    new ApiResponse(code = 400, message = "Bad request"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/vendors")
  def addNewVendor = {
    post {
      pathPrefix("vendors") {
        pathEndOrSingleSlash {
          entity(as[ProductVendor]) {
            case vendor@ProductVendor(title,_) =>
              val vendorAddition = productVendorService.addNewVendor(vendor)
              onSuccess(vendorAddition) { newId =>
                extractUri { uri =>
                  respondWithHeader(Location(uri + "/" + newId)) {
                    complete(StatusCodes.Created, ProductVendor(title, newId))
                  }
                }
              }
            case _ => complete(StatusCodes.BadRequest)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates Product Vendor", notes = "", nickname = "updateVendor", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "vendor", value = "Vendor For Update", required = true, dataType = "org.kolokolov.model.ProductVendor", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 205, message = "Vendor has been updated"),
    new ApiResponse(code = 400, message = "Bad request"),
    new ApiResponse(code = 404, message = "Vendor was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/vendors")
  def updateVendor = {
    put {
      pathPrefix("vendors") {
        pathEndOrSingleSlash {
          entity(as[ProductVendor]) {
            case vendor@ProductVendor(_,id) =>
              val vendorUpdating = productVendorService.updateVendor(vendor)
              onSuccess(vendorUpdating) {
                case 1 => complete(StatusCodes.ResetContent)
                case _ => complete(StatusCodes.NotFound, s"Vendor with ID: $id was not found")
              }
            case _ => complete(StatusCodes.BadRequest)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Returns Product Vendor By ID", notes = "", nickname = "getVendorById", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "vendorId", value = "Vendor ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Vendor by ID", response = classOf[ProductVendor]),
    new ApiResponse(code = 400, message = "Bad request"),
    new ApiResponse(code = 404, message = "Vendor was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/vendors/{vendorId}")
  def getVendorById = {
    get{
      pathPrefix("vendors" / IntNumber) { vendorId =>
        pathEndOrSingleSlash {
          val vendorRetrieval = productVendorService.getVendorById(vendorId)
          onSuccess(vendorRetrieval) {
            case Some(vendor) => complete(vendor)
            case None => complete(StatusCodes.NotFound, s"Vendor with ID: $vendorId was not found")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Deletes Product Vendor By ID", notes = "", nickname = "deleteVendorById", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "vendorId", value = "Vendor ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 205, message = "Vendor has been deleted"),
    new ApiResponse(code = 404, message = "Vendor was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/vendors/{vendorId}")
  def deleteVendorById = {
    delete {
      pathPrefix("vendors" / IntNumber) { vendorId =>
        pathEndOrSingleSlash {
          val vendorDeleting = productVendorService.deleteVendor(vendorId)
          onSuccess(vendorDeleting) {
            case 1 => complete(StatusCodes.ResetContent)
            case _ => complete(StatusCodes.NotFound, s"Vendor with ID: $vendorId was not found")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Returns All Products", notes = "", nickname = "getAllProducts", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "All products", response = classOf[Product], responseContainer = "List"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/products")
  def getAllProducts = {
    get{
      pathPrefix("products") {
        pathEndOrSingleSlash {
          val productRetrieval = productService.getAllProducts
          onSuccess(productRetrieval) {  products =>
            complete(products)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Adds New Product", notes = "", nickname = "addNewProduct", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "product", value = "New Product", required = true, dataType = "org.kolokolov.model.Product", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "New product has been added"),
    new ApiResponse(code = 400, message = "Bad request"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/products")
  def addNewProduct = {
    post {
      pathPrefix("products") {
        pathEndOrSingleSlash {
          entity(as[Product]) {
            case product@Product(name,category,vendor,_) =>
            val productAddition = productService.addNewProduct(product)
            onSuccess(productAddition) {
              case -1 => complete(StatusCodes.BadRequest, s"Product with category ID: $category and vendor ID: $vendor cannot be added")
              case newId => {
                extractUri { uri =>
                  respondWithHeader(Location(uri + "/" + newId)) {
                    complete(StatusCodes.Created, Product(name,category,vendor,newId))
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates Product", notes = "", nickname = "updateProduct", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "product", value = "Product For Update", required = true, dataType = "org.kolokolov.model.Product", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 205, message = "Product has been updated"),
    new ApiResponse(code = 400, message = "Bad request"),
    new ApiResponse(code = 404, message = "Product was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/products")
  def updateProduct = {
    put {
      pathPrefix("products") {
        pathEndOrSingleSlash {
          entity(as[Product]) {
            case product@Product(_,category,vendor,id) =>
              val productUpdating = productService.updateProduct(product)
              onSuccess(productUpdating) {
                case 1 => complete(StatusCodes.ResetContent)
                case -1 => complete(StatusCodes.BadRequest, s"Product has illegal category ID: $category or vandor ID: $vendor")
                case _ => complete(StatusCodes.NotFound, s"Product with ID: $id was not found")
              }
            case _ => complete(StatusCodes.BadRequest)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Returns Product ID", notes = "", nickname = "getProductById", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "productId", value = "Product ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Product by ID", response = classOf[ProductVendor]),
    new ApiResponse(code = 404, message = "Product was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/products/{productId}")
  def getProductById = {
    get{
      pathPrefix("products" / IntNumber) { productId =>
        pathEndOrSingleSlash {
          val productRetrieval = productService.getProductById(productId)
          onSuccess(productRetrieval) {
            case Some(product) => complete(product)
            case None => complete(StatusCodes.NotFound, s"Product with ID: $productId was not found")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Deletes Product By ID", notes = "", nickname = "deleteProductById", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "productId", value = "Product ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 205, message = "Product has been deleted"),
    new ApiResponse(code = 404, message = "Product was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/products/{productId}")
  def deleteProductById = {
    delete {
      pathPrefix("products" / IntNumber) { productId =>
        pathEndOrSingleSlash {
          val productDeleting = productService.deleteProduct(productId)
          onSuccess(productDeleting) {
            case 1 => complete(StatusCodes.ResetContent)
            case _ => complete(StatusCodes.NotFound, s"Product with ID: $productId was not found")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Returns All Orders", notes = "", nickname = "getAllOrders", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "All Orders", response = classOf[Order], responseContainer = "List"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/orders")
  def getAllOrders = {
    get{
      pathPrefix("orders") {
        pathEndOrSingleSlash {
          val ordersRetrieval = orderService.getAllOrders
          onSuccess(ordersRetrieval) { orders =>
            complete(orders)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates New Order", notes = "", nickname = "createNewOrder", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "order", value = "New Order", required = true, dataType = "org.kolokolov.model.Order", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "New order has been created"),
    new ApiResponse(code = 400, message = "Bad request"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/orders")
  def createNewOrder = {
    post {
      pathPrefix("orders") {
        pathEndOrSingleSlash {
          entity(as[Order]) {
            case order@Order(customerId, status, _) =>
              val orderCreation = orderService.createNewOrder(order)
              onSuccess(orderCreation) {
                case -1 => complete(StatusCodes.BadRequest,s"Order with customer ID: $customerId cannot be created")
                case newId =>
                  extractUri { uri =>
                    respondWithHeader(Location(uri + "/" + newId)) {
                      complete(StatusCodes.Created, Order(customerId, status, newId))
                    }
                  }
              }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates Order Status", notes = "", nickname = "updateOrderStatus", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "vendor", value = "Vendor For Update", required = true, dataType = "org.kolokolov.model.Order", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 205, message = "Order status has been updated"),
    new ApiResponse(code = 400, message = "Bad request"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/orders")
  def updateOrderStatus = {
    put {
      pathPrefix("orders") {
        pathEndOrSingleSlash {
          entity(as[Order]) {
            case order@Order(customerId,_,id) =>
              val orderStatusUpdating = orderService.updateOrderStatus(order)
              onSuccess(orderStatusUpdating) {
                case 1 => complete(StatusCodes.ResetContent)
                case -1 => complete(StatusCodes.BadRequest, s"Order has illegal customer ID: $customerId")
                case _ => complete(StatusCodes.BadRequest, s"Order with ID: $id of customer with ID: $customerId was not found")
              }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Returns Order By ID", notes = "", nickname = "getOrderById", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "orderId", value = "Order ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Order by ID", response = classOf[Order]),
    new ApiResponse(code = 404, message = "Order was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/orders/{orderId}")
  def getOrderById = {
    get{
      pathPrefix("orders" / IntNumber) { orderId =>
        pathEndOrSingleSlash {
          val orderRetrieval = orderService.getOrderById(orderId)
          onSuccess(orderRetrieval) {
            case Some(order) => complete(order)
            case None => complete(StatusCodes.NotFound, s"Vendor with ID: $orderId was not found")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Deletes Order By ID", notes = "", nickname = "deleteOrderById", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "orderId", value = "Order ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 205, message = "Order has been deleted"),
    new ApiResponse(code = 404, message = "Order was not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/orders/{orderId}")
  def deleteOrderById = {
    delete {
      pathPrefix("orders" / IntNumber) { orderId =>
        pathEndOrSingleSlash {
          val orderDeleting = orderService.deleteOrder(orderId)
          onSuccess(orderDeleting) {
            case 1 => complete(StatusCodes.ResetContent)
            case _ => complete(StatusCodes.NotFound, s"Order with ID: $orderId was not found")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Returns All Items Of Order With ID", notes = "", nickname = "getAllItemsOfOrder", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "orderId", value = "Order ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "All item of order with ID", response = classOf[OrderItem], responseContainer = "List"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/orders/{orderId}/items")
  def getAllItemsOfOrder = {
    get{
      pathPrefix("orders" / IntNumber / "items") { orderId =>
        pathEndOrSingleSlash {
          val itemsRetrieval = orderService.getItemsByOrderId(orderId)
          onSuccess(itemsRetrieval) { items =>
            complete(items)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Add Order Item To Order", notes = "", nickname = "addItemToOrder", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "orderId", value = "Order ID", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "item", value = "New Item", required = true, dataType = "org.kolokolov.model.OrderItem", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "New item has been added to order"),
    new ApiResponse(code = 400, message = "Bad request"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/orders/{orderId}/items")
  def addItemToOrder = {
    post {
      pathPrefix("orders" / IntNumber / "items") { orderId =>
        pathEndOrSingleSlash {
          entity(as[OrderItem]) {
            case item@OrderItem(order,product,quantity,_) if order == orderId =>
              val orderCreation = orderService.addNewItem(item)
              onSuccess(orderCreation) {
                case -1 => complete(StatusCodes.BadRequest, s"Order item has illegal product ID: $product")
                case newId =>
                  extractUri { uri =>
                    respondWithHeader(Location(uri + "/" + newId)) {
                      complete(StatusCodes.Created, OrderItem(order, product, quantity, newId))
                    }
                  }
              }
            case _ => complete(StatusCodes.BadRequest, s"Wrong order ID in ite")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Update Product Quantity In Order Item", notes = "", nickname = "updateProductQuantityInItem", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "orderId", value = "Order ID", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "item", value = "Updated Item", required = true, dataType = "org.kolokolov.model.OrderItem", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Product quantity has been updated"),
    new ApiResponse(code = 400, message = "Bad request"),
    new ApiResponse(code = 404, message = "Item was not found in order"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/orders/{orderId}/items")
  def updateProductQuantityInItem = {
    put {
      pathPrefix("orders" / IntNumber / "items") { orderId =>
        pathEndOrSingleSlash {
          entity(as[OrderItem]) {
            case item@OrderItem(itemOrderId, productId, _, id) if itemOrderId == orderId =>
              val itemUpdating = orderService.updateProductQuantity(item)
              onSuccess(itemUpdating) {
                case 1 => complete(StatusCodes.ResetContent)
                case _ => complete(StatusCodes.NotFound, s"Item with ID: $id and product ID: $productId was not found in order with ID: $orderId")
              }
            case _ => complete(StatusCodes.BadRequest, s"Wrong order ID in item")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Remove Product Item From Order", notes = "", nickname = "removeItemFromOrder", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "orderId", value = "Order ID", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "itemId", value = "Item ID", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 205, message = "Item has been removed from order"),
    new ApiResponse(code = 404, message = "Item was not found in order"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  @Path("/orders/{orderId}/items/{itemId}")
  def removeItemFromOrder = {
    delete {
      pathPrefix("orders" / IntNumber / "items" / IntNumber) { (orderId, itemId) =>
        pathEndOrSingleSlash {
          val itemRemoval = orderService.removeItemFromOrder(itemId, orderId)
          onSuccess(itemRemoval) {
            case 1 => complete(StatusCodes.ResetContent)
            case _ => complete(StatusCodes.NotFound, s"Item with ID: $itemId was not found in order with ID: $orderId")
          }
        }
      }
    }
  }
}