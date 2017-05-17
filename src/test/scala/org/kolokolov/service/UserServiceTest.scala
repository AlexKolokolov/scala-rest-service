package org.kolokolov.service

import org.kolokolov.model.User
import org.kolokolov.repo.H2Database
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Kolokolov on 5/10/17.
  */
class UserServiceTest extends AsyncFunSuite
  with Matchers
  with BeforeAndAfterEach {

  private val userService = new UserService(H2Profile)

  private val dbTestHelper = new TestDBCreator with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  test("getAllEntities should return Seq(User(Bob Marley,1), User(Tom Waits,2), User(Guy Pearce,3))") {
    userService.getAllUsers.map {
      result => result shouldEqual Seq(User("Bob Marley",1), User("Tom Waits",2), User("Guy Pearce",3))
    }
  }
}