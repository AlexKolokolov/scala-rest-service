package org.kolokolov.service

import org.kolokolov.model.Product
import org.kolokolov.repo.H2Database
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * Created by Kolokolov on 5/10/17.
  */
class ProductServiceTest extends AsyncFunSuite
  with Matchers
  with BeforeAndAfterEach {

  private val productService = new ProductService(H2Profile)

  import productService._

  private val dbTestHelper = new TestDBCreator with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  test("getAllProducts should return Seq(Product(MP5,1,1,1),Product(UPS,2,1,2),Product(P90,1,2,3),Product(FiveSeven,2,2,4)))") {
    getAllProducts.map { result =>
      result shouldEqual Seq(Product("MP5",1,1,1),Product("UPS",2,1,2),Product("P90",1,2,3),Product("FiveSeven",2,2,4))
    }
  }

  test("getProductById(1) should return Some(Product(MP5,1,1,1))") {
    getProductById(1).map { result =>
      result shouldEqual Some(Product("MP5",1,1,1))
    }
  }

  test("getVendorById(5) should return None") {
    getProductById(5).map { result =>
      result shouldEqual None
    }
  }

  test("getProductById(1) should return None after deleteProduct(1)") {
    deleteProduct(1).flatMap { delResult =>
        delResult shouldEqual 1
        getProductById(1).map { result =>
          result shouldEqual None
      }
    }
  }

  test("deleteProduct(5) should return 0") {
    deleteProduct(5).map { result =>
      result shouldEqual 0
    }
  }

  test("getProductById(5) should return Some(Product(UMP,1,1,5)) after addNewProduct(Product(UMP,1,1))") {
    addNewProduct(Product("UMP",1,1)).flatMap { saveResult =>
      saveResult shouldEqual 5
      getProductById(5).map { result =>
        result shouldEqual Some(Product("UMP",1,1,5))
      }
    }
  }

  test("addNewProduct(Product(UMP,3,1)) should return -1") {
    addNewProduct(Product("UMP",3,1)).map { saveResult =>
      saveResult shouldEqual -1
    }
  }

  test("addNewProduct(Product(UMP,1,3)) should return -1") {
    addNewProduct(Product("UMP",1,3)).map { saveResult =>
      saveResult shouldEqual -1
    }
  }

  test("getProductById(1) should return Some(Product(UMP,1)) after updateProduct(Product(UMP,1))") {
    updateProduct(Product("UMP",1,1,1)).flatMap { updateResult =>
      updateResult shouldEqual 1
      getProductById(1).map { result =>
        result shouldEqual Some(Product("UMP",1,1,1))
      }
    }
  }

  test("updateProduct(Product(UMP,1,1,5)) should return 0") {
    updateProduct(Product("UMP",1,1,5)).map { result =>
      result shouldEqual 0
    }
  }

  test("updateProduct(Product(UPS,3,1,1)) should return -1") {
    updateProduct(Product("UPS",3,1,1)).map { result =>
      result shouldEqual -1
    }
  }

  test("updateProduct(Product(UPS,1,3,1)) should return -1") {
    updateProduct(Product("UMP",1,3,1)).map { result =>
      result shouldEqual -1
    }
  }
}