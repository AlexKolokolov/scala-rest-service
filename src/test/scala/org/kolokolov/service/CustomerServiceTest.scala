package org.kolokolov.service

import org.kolokolov.model.Customer
import org.kolokolov.repo.H2Database
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import concurrent.ExecutionContext.Implicits.global

/**
  * Created by Kolokolov on 5/10/17.
  */
class CustomerServiceTest extends AsyncFunSuite
  with Matchers
  with BeforeAndAfterEach {

  private val customerService = new CustomerService(H2Profile)

  import customerService._

  private val dbTestHelper = new TestDBCreator with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  test("getAllCustomers should return Seq(Customer(Bob Marley,1), Customer(Tom Waits,2))") {
    getAllCustomers.map { result =>
      result shouldEqual Seq(Customer("Bob Marley",1), Customer("Tom Waits",2))
    }
  }

  test("getCustomerById(1) should return Some(Customer(Bob Marley,1))") {
    getCustomerById(1).map { result =>
      result shouldEqual Some(Customer("Bob Marley",1))
    }
  }

  test("getCustomerById(3) should return None") {
    getCustomerById(3).map { result =>
      result shouldEqual None
    }
  }

  test("getUserById(1) should return None after deleteCustomer(1)") {
    deleteCustomer(1).flatMap { delResult =>
        delResult shouldEqual 1
        getCustomerById(1).map { result =>
          result shouldEqual None
      }
    }
  }

  test("deleteCustomer(3) should return 0") {
    deleteCustomer(3).map { result =>
      result shouldEqual 0
    }
  }

  test("getCustomerById(3) should return Some(Customer(Marlon Brando, 3)) after addNewCustomer(Customer(Marlon Brando))") {
    addNewCustomer(Customer("Marlon Brando")).flatMap { saveResult =>
      saveResult shouldEqual 3
      getCustomerById(3).map { result =>
        result shouldEqual Some(Customer("Marlon Brando", 3))
      }
    }
  }

  test("getCustomerById(1) should return Some(Customer(Marlon Brando, 1)) after updateCustomer(Customer(Marlon Brando, 1))") {
    updateCustomer(Customer("Marlon Brando",1)).flatMap { updateResult =>
      updateResult shouldEqual 1
      getCustomerById(1).map { result =>
        result shouldEqual Some(Customer("Marlon Brando", 1))
      }
    }
  }

  test("updateUser(Customer(Marlon Brando, 3)) should return 0") {
    updateCustomer(Customer("Marlon Brando",3)).map { result =>
      result shouldEqual 0
    }
  }
}