package org.kolokolov.model

/**
  * Created by Kolokolov on 24.05.2017.
  */
case class OrderItem(orderId: Int, productId: Int, quantity: Int, id: Int = 0) extends Identifiable {
  require(quantity > 0, "Quantity should be positive")
}
