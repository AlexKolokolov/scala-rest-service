package org.kolokolov.rest.integration

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.kolokolov.repo.H2Database
import org.kolokolov.rest.{JsonSupport, RestController}
import org.kolokolov.service.{CommentService, MessageService, TestDBCreator, UserService}
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import akka.http.scaladsl.model._
import org.kolokolov.model.User
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Kolokolov on 11.05.2017.
  */
class RestControllerTest extends AsyncFunSuite
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfterEach
  with JsonSupport{

  private val route = new RestController(new UserService(H2Profile), new MessageService(H2Profile), new CommentService(H2Profile), ActorSystem("test-actor-system")).rootRoute

  val dbTestHelper = new TestDBCreator with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  test("should return all users") {
    Get("/webapi/users") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "[{\"name\":\"Bob Marley\",\"id\":1},{\"name\":\"Tom Waits\",\"id\":2},{\"name\":\"Guy Pearce\",\"id\":3}]"
    }
  }

  test("should return {name:Bob Marley,id:1}") {
    Get("/webapi/users/1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "{\"name\":\"Bob Marley\",\"id\":1}"
    }
  }

  test("should return 'User User(Ron Perlman,0) has been saved'") {
    Post("/webapi/users",User("Ron Perlman")) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[String] shouldEqual "User User(Ron Perlman,0) has been saved"
    }
  }

  test("should return 'User with ID: 1 has been deleted'") {
    Delete("/webapi/users/1") ~> route ~> check {
      status shouldEqual StatusCodes.Accepted
      responseAs[String] shouldEqual "User with ID: 1 has been deleted"
    }
  }

  test("return 'User with ID: 2 has been updated'") {
    Put("/webapi/users",User("Ron Perlman",2)) ~> route ~> check {
      status shouldEqual StatusCodes.ResetContent
      responseAs[String] shouldEqual "User with ID: 2 has been updated"
    }
  }

  test("should return 'User with ID: 4 was not found' after illegal get") {
    Get("/webapi/users/4") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "User with ID: 4 was not found"
    }
  }

  test("should return 'User with ID: 4 was not found' after illegal put") {
    Put("/webapi/users",User("Ron Perlman",4)) ~> route ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[String] shouldEqual "User with ID: 4 was not found"
    }
  }

  test("should return 'User with ID: 4 was not found' after illegal delete") {
    Delete("/webapi/users/4") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "User with ID: 4 was not found"
    }
  }
}