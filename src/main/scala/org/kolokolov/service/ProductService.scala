package org.kolokolov.service

import org.kolokolov.model.Product
import org.kolokolov.repo.{DatabaseProfile, ProductCRUDModule}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class ProductService(override val profile: JdbcProfile) extends ProductCRUDModule with DatabaseProfile {

  def getAllProducts: Future[Seq[Product]] = ProductCRUD.getAll
  def getProductById(id: Int): Future[Option[Product]] = ProductCRUD.getById(id)
  def addNewProduct(product: Product): Future[Int] = ProductCRUD.save(product)
  def deleteProduct(id: Int): Future[Int] = ProductCRUD.delete(id)
  def updateProduct(product: Product): Future[Int] = ProductCRUD.update(product)
}
