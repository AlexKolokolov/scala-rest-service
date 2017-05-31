package org.kolokolov.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.kolokolov.model.OrderStatus.OrderStatus
import org.kolokolov.model._
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

/**
  * Created by Kolokolov on 11.05.2017.
  */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit object orderStatusFormat extends RootJsonFormat[OrderStatus] {
    override def read(json: JsValue) = OrderStatus.withName(json.convertTo[String])
    override def write(obj: OrderStatus) = JsString(obj.toString)
  }
  implicit val userFormat: RootJsonFormat[Customer] = jsonFormat2(Customer)
  implicit val orderFormat: RootJsonFormat[Order] = jsonFormat3(Order)
  implicit val orderItemFormat: RootJsonFormat[OrderItem] = jsonFormat4(OrderItem)
  implicit val productCategoryFormat: RootJsonFormat[ProductCategory] = jsonFormat2(ProductCategory)
  implicit val productVendorFormat: RootJsonFormat[ProductVendor] = jsonFormat2(ProductVendor)
  implicit val productFormat: RootJsonFormat[Product] = jsonFormat4(Product)

}
