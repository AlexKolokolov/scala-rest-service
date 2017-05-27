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
class UserServiceTest extends AsyncFunSuite
  with Matchers
  with BeforeAndAfterEach {

  private val userService = new UserService(H2Profile)

  import userService._

  private val dbTestHelper = new TestDBCreator with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  test("getAllUsers should return Seq(User(Bob Marley,1), User(Tom Waits,2), User(Guy Pearce,3))") {
    getAllUsers.map { result =>
      result shouldEqual Seq(Customer("Bob Marley",1), Customer("Tom Waits",2), Customer("Guy Pearce",3))
    }
  }

  test("getUserById(1) should return Some(User(Bob Marley,1))") {
    getUserById(1).map { result =>
      result shouldEqual Some(Customer("Bob Marley",1))
    }
  }

  test("getUserById(4) should return None") {
    getUserById(4).map { result =>
      result shouldEqual None
    }
  }

  test("getUserById(1) should return None after deleteUser(1)") {
    deleteUser(1).flatMap { delResult =>
        delResult shouldEqual 1
        getUserById(1).map { result =>
          result shouldEqual None
      }
    }
  }

  test("deleteUser(4) should return 0") {
    deleteUser(4).map {
      result => result shouldEqual 0
    }
  }

  test("getUserById(4) should return Some(User(Marlon Brando, 4)) after saveUser(User(Marlon Brando))") {
    saveUser(Customer("Marlon Brando")).flatMap { saveResult =>
      saveResult shouldEqual 1
      getUserById(4).map { result =>
        result shouldEqual Some(Customer("Marlon Brando", 4))
      }
    }
  }

  test("getUserById(1) should return Some(User(Marlon Brando, 1)) after updateUser(User(Marlon Brando, 1))") {
    updateUser(Customer("Marlon Brando",1)).flatMap { updateResult =>
      updateResult shouldEqual 1
      getUserById(1).map { result =>
        result shouldEqual Some(Customer("Marlon Brando", 1))
      }
    }
  }

  test("updateUser(User(Marlon Brando, 4)) should return 0") {
    updateUser(Customer("Marlon Brando",4)).map { result =>
      result shouldEqual 0
    }
  }
}