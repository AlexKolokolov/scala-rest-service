package org.kolokolov.service

import org.kolokolov.model.ProductCategory
import org.kolokolov.repo.{DatabaseProfile, ProductCategoryCRUDModule}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Kolokolov on 10.05.2017.
  */
class ProductCategoryService(override val profile: JdbcProfile) extends ProductCategoryCRUDModule with DatabaseProfile {

  def getAllCategories: Future[Seq[ProductCategory]] = ProductCategoryCRUD.getAll
  def getCategoryById(id: Int): Future[Option[ProductCategory]] = ProductCategoryCRUD.getById(id)
  def addNewCategory(category: ProductCategory): Future[Int] = ProductCategoryCRUD.save(category)
  def deleteCategory(id: Int): Future[Int] = ProductCategoryCRUD.delete(id)
  def updateCategory(category: ProductCategory): Future[Int] = ProductCategoryCRUD.update(category)
}
