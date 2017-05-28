package org.kolokolov.service

import org.kolokolov.model.ProductCategory
import org.kolokolov.repo.H2Database
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * Created by Kolokolov on 5/10/17.
  */
class ProductCategoryServiceTest extends AsyncFunSuite
  with Matchers
  with BeforeAndAfterEach {

  private val categoryService = new ProductCategoryService(H2Profile)

  import categoryService._

  private val dbTestHelper = new TestDBCreator with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  test("getAllCategories should return Seq(ProductCategory(SMG,1), ProductCategory(Sidearm,2))") {
    getAllCategories.map { result =>
      result shouldEqual Seq(ProductCategory("SMG",1), ProductCategory("Sidearm",2))
    }
  }

  test("getCategoryById(1) should return Some(Customer(Bob Marley,1))") {
    getCategoryById(1).map { result =>
      result shouldEqual Some(ProductCategory("SMG",1))
    }
  }

  test("getCategoryById(3) should return None") {
    getCategoryById(3).map { result =>
      result shouldEqual None
    }
  }

  test("getCategoryById(1) should return None after deleteCategory(1)") {
    deleteCategory(1).flatMap { delResult =>
        delResult shouldEqual 1
        getCategoryById(1).map { result =>
          result shouldEqual None
      }
    }
  }

  test("deleteCategory(3) should return 0") {
    deleteCategory(3).map { result =>
      result shouldEqual 0
    }
  }

  test("getCategoryById(3) should return Some(ProductCategory(Rifle, 3)) after addNewCategory(ProductCategory(Rifle))") {
    addNewCategory(ProductCategory("Rifle")).flatMap { saveResult =>
      saveResult shouldEqual 3
      getCategoryById(3).map { result =>
        result shouldEqual Some(ProductCategory("Rifle", 3))
      }
    }
  }

  test("getCategoryById(1) should return Some(ProductCategory(Rifle,1)) after updateCategory(ProductCategory(Rifle))") {
    updateCategory(ProductCategory("Rifle",1)).flatMap { updateResult =>
      updateResult shouldEqual 1
      getCategoryById(1).map { result =>
        result shouldEqual Some(ProductCategory("Rifle", 1))
      }
    }
  }

  test("updateCategory(ProductCategory(Rifle, 3)) should return 0") {
    updateCategory(ProductCategory("Rifle",3)).map { result =>
      result shouldEqual 0
    }
  }
}