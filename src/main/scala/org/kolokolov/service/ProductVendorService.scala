package org.kolokolov.service

import org.kolokolov.model.ProductVendor
import org.kolokolov.repo.{DatabaseProfile, ProductVendorCRUDModule}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Kolokolov on 10.05.2017.
  */
class ProductVendorService(override val profile: JdbcProfile) extends ProductVendorCRUDModule with DatabaseProfile {

  def getAllVendors: Future[Seq[ProductVendor]] = ProductVendorCRUD.getAll
  def getVendorById(id: Int): Future[Option[ProductVendor]] = ProductVendorCRUD.getById(id)
  def addNewVendor(vendor: ProductVendor): Future[Int] = ProductVendorCRUD.save(vendor)
  def deleteVendor(id: Int): Future[Int] = ProductVendorCRUD.delete(id)
  def updateVendor(vendor: ProductVendor): Future[Int] = ProductVendorCRUD.update(vendor)
}
