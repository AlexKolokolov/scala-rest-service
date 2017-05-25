package org.kolokolov.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.kolokolov.model._
import org.kolokolov.repo.DatabaseProfile
import org.kolokolov.service._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by Kolokolov on 11.05.2017.
  */
class ShopRestController(system: ActorSystem) extends JsonSupport {

  this: DatabaseProfile =>

  lazy val customerService = new CustomerService(profile)
  lazy val productService = new ProductService(profile)
  lazy val productCategoryService = new ProductCategoryService(profile)
  lazy val productVendorService = new ProductVendorService(profile)
  lazy val orderService = new OrderService(profile)

  implicit val executionContext = system.dispatcher

  private val logger = LoggerFactory.getLogger(this.getClass)

  val rootRoute = Route {
    pathPrefix("webapi") {
      customerRoute ~ productVendorRoute ~ productCategoryRoute ~ productRoute ~ orderRoute
    }
  }

  private val customerRoute: Route =
    pathPrefix("customers") {
      pathEndOrSingleSlash {
        get {
          // GET /webapi/customers - Get all customers
          val customerFuture: Future[Seq[Customer]] = customerService.getAllCustomers
          onComplete(customerFuture) {
            case Success(customers) => complete(customers)
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        } ~
        post {
          // POST /webapi/customers - Add new customer
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
        } ~
        put {
          // PUT /webapi/customers - Update customer
          entity(as[Customer]) { customer =>
            val updatedCustomer = customerService.updateCustomer(customer)
            onComplete(updatedCustomer) {
              case Success(1) => complete(StatusCodes.ResetContent, s"Customer with ID: ${customer.id} has been updated")
              case Success(_) => complete(StatusCodes.BadRequest, s"Customer with ID: ${customer.id} was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      } ~
      pathPrefix(IntNumber) { customerId =>
        pathEndOrSingleSlash {
          get {
            // GET /webapi/customer/1 - Get customer by ID
            val userFuture: Future[Option[Customer]] = customerService.getCustomerById(customerId)
            onComplete(userFuture) {
              case Success(Some(customer)) => complete(customer)
              case Success(None) => complete(StatusCodes.NotFound, s"Customer with ID: $customerId was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          } ~
          delete {
            // DELETE /webapi/customers/1 - Delete customer by ID
            val deletedCustomer = customerService.deleteCustomer(customerId)
            onComplete(deletedCustomer) {
              case Success(1) => complete(StatusCodes.Accepted, s"Customer with ID: $customerId has been deleted")
              case Success(_) => complete(StatusCodes.NotFound, s"Customer with ID: $customerId was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        pathPrefix("orders") {
          pathEndOrSingleSlash {
            get {
              // GET /webapi/customers/1/orders - Get all orders of a particular customer
              val ordersFuture: Future[Seq[Order]] = orderService.getOrdersByCustomerId(customerId)
              onComplete(ordersFuture) {
                case Success(orders) => complete(orders)
                case Failure(ex) => {
                  logger.error(ex.getStackTrace.mkString)
                  complete(StatusCodes.InternalServerError)
                }
              }
            }
          } ~
          pathPrefix(IntNumber) { orderId =>
            pathEndOrSingleSlash {
              get {
                // GET /webapi/customers/1/order/1 - Get order by ID of a particular customer
                val orderFuture: Future[Option[Order]] = orderService.getCustomersOrderById(orderId,customerId)
                onComplete(orderFuture) {
                  case Success(Some(order)) => complete(order)
                  case Success(None) => complete(StatusCodes.NotFound, s"Order with ID: $orderId by user with ID: $customerId not found")
                  case Failure(ex) => {
                    logger.error(ex.getStackTrace.mkString)
                    complete(StatusCodes.InternalServerError)
                  }
                }
              }
            } ~
            pathPrefix("items") {
              pathEndOrSingleSlash {
                get {
                  // GET /webapi/customers/1/orders/1/items - Get all items of a particular order of a particular customer
                  val itemsFuture = orderService.getAllItemsOfCustomerOrderById(orderId,customerId)
                  onComplete(itemsFuture) {
                    case Success(items) => complete(items)
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
    }

  private val productVendorRoute: Route =
    pathPrefix("vendors") {
      pathEndOrSingleSlash {
        get {
          // GET /webapi/vendors - Get all vendors
          val futureVendors = productVendorService.getAllVendors
          onComplete(futureVendors) {
            case Success(vendors) => complete(vendors)
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        } ~
        post {
          // POST /webapi/vendors - Add new vendor
          entity(as[ProductVendor]) { vendor =>
            val savedVendor = productVendorService.addNewVendor(vendor)
            onComplete(savedVendor) {
              case Success(1) => complete(StatusCodes.Created, s"New vendor $vendor has been added")
              case Success(_) => complete(StatusCodes.BadRequest)
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        put {
          // PUT /webapi/vendor - Update vendor
          entity(as[ProductVendor]) { vendor =>
            val updatedVendor = productVendorService.updateVendor(vendor)
            onComplete(updatedVendor) {
              case Success(1) => complete(StatusCodes.ResetContent, s"Vendor with ID: ${vendor.id} has been updated")
              case Success(_) => complete(StatusCodes.BadRequest, s"Vendor with ID: ${vendor.id} was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      }~
      pathPrefix(IntNumber) { vendorId =>
        pathEndOrSingleSlash {
          get {
            // GET /webapi/vendors/1 - Get vendor by ID
            val vendorFuture: Future[Option[ProductVendor]] = productVendorService.getVendorById(vendorId)
            onComplete(vendorFuture) {
              case Success(Some(vendor)) => complete(vendor)
              case Success(None) => complete(StatusCodes.NotFound, s"Vendor with ID: $vendorId was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          } ~
          delete {
            // DELETE /webapi/vendors/1 - Delete vendor by ID
            val deletedVendor = productVendorService.deleteVendor(vendorId)
            onComplete(deletedVendor) {
              case Success(1) => complete(StatusCodes.Accepted, s"Vendor with ID: $vendorId has been deleted")
              case Success(_) => complete(StatusCodes.NotFound, s"Vendor with ID: $vendorId was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      }
    }

  private val productCategoryRoute: Route =
    pathPrefix("categories") {
      pathEndOrSingleSlash {
        get {
          // GET /webapi/categories - Get all categories
          val futureCategory = productCategoryService.getAllCategories
          onComplete(futureCategory) {
            case Success(categories) => complete(categories)
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        } ~
        post {
          // POST /webapi/categories - Add new category
          entity(as[ProductCategory]) { category =>
            val savedCategory = productCategoryService.addNewCategory(category)
            onComplete(savedCategory) {
              case Success(1) => complete(StatusCodes.Created, s"New category $category has been added")
              case Success(_) => complete(StatusCodes.BadRequest)
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        put {
          // PUT /webapi/categories - Update category
          entity(as[ProductCategory]) { category =>
            val updatedCategory = productCategoryService.updateCategory(category)
            onComplete(updatedCategory) {
              case Success(1) => complete(StatusCodes.ResetContent, s"Category with ID: ${category.id} has been updated")
              case Success(_) => complete(StatusCodes.BadRequest, s"Category with ID: ${category.id} was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      }~
      pathPrefix(IntNumber) { categoryId =>
        pathEndOrSingleSlash {
          get {
            // GET /webapi/categories/1 - Get category by ID
            val categoryFuture: Future[Option[ProductCategory]] = productCategoryService.getCategoryById(categoryId)
            onComplete(categoryFuture) {
              case Success(Some(category)) => complete(category)
              case Success(None) => complete(StatusCodes.NotFound, s"Category with ID: $categoryId was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          } ~
          delete {
            // DELETE /webapi/vendors/1 - Delete vendor by ID
            val deletedCategory = productCategoryService.deleteCategory(categoryId)
            onComplete(deletedCategory) {
              case Success(1) => complete(StatusCodes.Accepted, s"Category with ID: $categoryId has been deleted")
              case Success(_) => complete(StatusCodes.NotFound, s"Category with ID: $categoryId was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      }
    }

  private val productRoute: Route =
    pathPrefix("products") {
      pathEndOrSingleSlash {
        get {
          // GET /webapi/products - Get all products
          val futureProducts = productService.getAllProducts
          onComplete(futureProducts) {
            case Success(products) => complete(products)
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        } ~
        post {
          // POST /webapi/products - Add new product
          entity(as[Product]) { product =>
            val savedProduct = productService.addNewProduct(product)
            onComplete(savedProduct) {
              case Success(1) => complete(StatusCodes.Created, s"New product $product has been added")
              case Success(_) => complete(StatusCodes.BadRequest)
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        put {
          // PUT /webapi/products - Update product
          entity(as[Product]) { product =>
            val updatedProduct = productService.updateProduct(product)
            onComplete(updatedProduct) {
              case Success(1) => complete(StatusCodes.ResetContent, s"Product with ID: ${product.id} has been updated")
              case Success(_) => complete(StatusCodes.BadRequest, s"Product with ID: ${product.id} was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      }~
      pathPrefix(IntNumber) { productId =>
        pathEndOrSingleSlash {
          get {
            // GET /webapi/products/1 - Get product by ID
            val productFuture: Future[Option[Product]] = productService.getProductById(productId)
            onComplete(productFuture) {
              case Success(Some(product)) => complete(product)
              case Success(None) => complete(StatusCodes.NotFound, s"Product with ID: $productId was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          } ~
          delete {
            // DELETE /webapi/products/1 - Delete product by ID
            val deletedProduct = productService.deleteProduct(productId)
            onComplete(deletedProduct) {
              case Success(1) => complete(StatusCodes.Accepted, s"Product with ID: $productId has been deleted")
              case Success(_) => complete(StatusCodes.NotFound, s"Product with ID: $productId was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      }
    }

  private val orderRoute: Route =
    pathPrefix("orders") {
      pathEndOrSingleSlash {
        get {
          // GET /webapi/orders - Get all orders
          val futureOrders = orderService.getAllOrders
          onComplete(futureOrders) {
            case Success(orders) => complete(orders)
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        } ~
        post {
          // POST /webapi/orders - Create a new order
          entity(as[Order]) { order =>
            val createdOrder = orderService.createNewOrder(order)
            onComplete(createdOrder) {
              case Success(1) => complete(StatusCodes.Created, s"New order $order has been created")
              case Success(_) => complete(StatusCodes.BadRequest)
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        put {
          // PUT /webapi/orders - Update order status
          entity(as[Order]) { order =>
            val updatedOrder = orderService.updateOrderStatus(order)
            onComplete(updatedOrder) {
              case Success(1) => complete(StatusCodes.ResetContent, s"Order with ID: ${order.id} has been updated")
              case Success(_) => complete(StatusCodes.BadRequest, s"Order with ID: ${order.id} was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      }~
      pathPrefix(IntNumber) { orderId =>
        pathEndOrSingleSlash {
          get {
            // GET /webapi/orders/1 - Get orders by ID
            val orderFuture: Future[Option[Order]] = orderService.getOrderById(orderId)
            onComplete(orderFuture) {
              case Success(Some(order)) => complete(order)
              case Success(None) => complete(StatusCodes.NotFound, s"Order with ID: $orderId was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          } ~
          delete {
            // DELETE /webapi/orders/1 - Delete order by ID
            val deletedOrder = orderService.deleteOrder(orderId)
            onComplete(deletedOrder) {
              case Success(1) => complete(StatusCodes.Accepted, s"Order with ID: $orderId has been deleted")
              case Success(_) => complete(StatusCodes.NotFound, s"Order with ID: $orderId was not found")
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        pathPrefix("items") {
          pathEndOrSingleSlash {
            get {
              // GET /webapi/orders/1/items - Get all items of a particular order
              val futureItems: Future[Seq[OrderItem]] = orderService.getItemsByOrderId(orderId)
              onComplete(futureItems) {
                case Success(items) => complete(items)
                case Failure(ex) => {
                  logger.error(ex.getStackTrace.mkString)
                  complete(StatusCodes.InternalServerError)
                }
              }
            } ~
            post {
              // POST /webapi/orders/1/items - Add new item to a particular order
              entity(as[OrderItem]) { item =>
                item.orderId match {
                  case `orderId` => {
                    val addedItem: Future[Int] = orderService.addNewItem(item)
                    onComplete(addedItem) {
                      case Success(1) => complete(StatusCodes.Created, s"New item $item has been added to order with ID $orderId")
                      case Success(_) => complete(StatusCodes.BadRequest)
                      case Failure(ex) => {
                        logger.error(ex.getStackTrace.mkString)
                        complete(StatusCodes.InternalServerError)
                      }
                    }
                  }
                  case _ => complete(StatusCodes.BadRequest)
                }
              }
            } ~
            put {
              // PUT /webapi/orders/1/items - Update item of a particular order
              entity(as[OrderItem]) { item =>
                item.orderId match {
                  case `orderId` => {
                    val updatedItem: Future[Int] = orderService.updateProductQuantity(item)
                    onComplete(updatedItem) {
                      case Success(1) => complete(StatusCodes.Created, s"Quantity of the item with ID ${item.id} in the order with ID $orderId has been changed to ${item.quantity}")
                      case Success(_) => complete(StatusCodes.NotFound, s"No item was found for possible update with item ${item  }")
                      case Failure(ex) => {
                        logger.error(ex.getStackTrace.mkString)
                        complete(StatusCodes.InternalServerError)
                      }
                    }
                  }
                  case _ => complete(StatusCodes.BadRequest)
                }
              }
            }
          } ~
          pathPrefix(IntNumber) { itemId =>
            pathEndOrSingleSlash {
              delete {
                // PUT /webapi/orders/1/items/1 - delete item by ID from a particular order
                val deletedItem = orderService.removeItemFromOrder(itemId, orderId)
                onComplete(deletedItem) {
                  case Success(1) => complete(StatusCodes.Created, s"Item with ID $itemId has been removed from the order with ID $orderId")
                  case Success(_) => complete(StatusCodes.NotFound, s"Item with ID $itemId was not found in the order with ID $orderId")
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
}

