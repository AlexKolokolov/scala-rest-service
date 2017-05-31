package org.kolokolov.service

import org.kolokolov.model.ProductVendor
import org.kolokolov.repo.H2Database
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * Created by Kolokolov on 5/10/17.
  */
class ProductVendorServiceTest extends AsyncFunSuite
  with Matchers
  with BeforeAndAfterEach {

  private val vendorService = new ProductVendorService(H2Profile)

  import vendorService._

  private val dbTestHelper = new TestDBCreator with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  test("getAllVendors should return Seq(ProductVendor(H&K,1), ProductVendor(FN,2))") {
    getAllVendors.map { result =>
      result shouldEqual Seq(ProductVendor("H&K",1), ProductVendor("FN",2))
    }
  }

  test("getVendorById(1) should return Some(ProductVendor(H&K,1))") {
    getVendorById(1).map { result =>
      result shouldEqual Some(ProductVendor("H&K",1))
    }
  }

  test("getVendorById(3) should return None") {
    getVendorById(3).map { result =>
      result shouldEqual None
    }
  }

  test("getVendorById(1) should return None after deleteVendor(1)") {
    deleteVendor(1).flatMap { delResult =>
        delResult shouldEqual 1
        getVendorById(1).map { result =>
          result shouldEqual None
      }
    }
  }

  test("deleteVendor(3) should return 0") {
    deleteVendor(3).map { result =>
      result shouldEqual 0
    }
  }

  test("getVendorById(3) should return Some(ProductVendor(IMI, 3)) after addNewVendor(ProductVendor(IMI))") {
    addNewVendor(ProductVendor("IMI")).flatMap { saveResult =>
      saveResult shouldEqual 3
      getVendorById(3).map { result =>
        result shouldEqual Some(ProductVendor("IMI", 3))
      }
    }
  }

  test("getVendorById(1) should return Some(ProductVendor(IMI,1)) after updateVendor(ProductVendor(IMI,1))") {
    updateVendor(ProductVendor("IMI",1)).flatMap { updateResult =>
      updateResult shouldEqual 1
      getVendorById(1).map { result =>
        result shouldEqual Some(ProductVendor("IMI", 1))
      }
    }
  }

  test("updateVendor(ProductVendor(IMI, 3)) should return 0") {
    updateVendor(ProductVendor("IMI",3)).map { result =>
      result shouldEqual 0
    }
  }
}