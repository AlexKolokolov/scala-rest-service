package org.kolokolov.model

/**
  * Created by Kolokolov on 24.05.2017.
  */
object OrderStatus extends Enumeration {
  type OrderStatus = Value
  val Created = Value("Created")
  val Confirmed = Value("Confirmed")
  val Suspended = Value("Suspended")
  val Delivered = Value("Delivered")
  val Aborted = Value("Aborted")
}