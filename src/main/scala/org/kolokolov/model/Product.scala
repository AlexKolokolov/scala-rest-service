package org.kolokolov.model

/**
  * Created by Kolokolov on 24.05.2017.
  */
case class Product(name: String, categoryId: Int, vendorId: Int, id: Int = 0) extends Identifiable
