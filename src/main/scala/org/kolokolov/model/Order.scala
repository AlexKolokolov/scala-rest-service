package org.kolokolov.model

import org.kolokolov.model.OrderStatus.OrderStatus

/**
  * Created by Kolokolov on 24.05.2017.
  */
case class Order(customerId: Int, status: OrderStatus, id: Int = 0) extends Identifiable