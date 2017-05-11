package org.kolokolov.rest.integration

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.kolokolov.model.Entity
import org.kolokolov.repo.H2Database
import org.kolokolov.rest.{JsonSupport, RestController}
import org.kolokolov.service.{EntityService, TestDBCreator}
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}
import akka.http.scaladsl.model._
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Kolokolov on 11.05.2017.
  */
class RestServiceTest extends FunSuite
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfterEach
  with JsonSupport{

  private val route = new RestController(new EntityService(H2Profile), ActorSystem("test-actor-system")).route

  val dbTestHelper = new TestDBCreator with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  test("return all entities") {
    Get("/webapi/entities") ~> route ~> check {
      responseAs[String] shouldEqual "[{\"name\":\"Mercury\",\"id\":1},{\"name\":\"Venus\",\"id\":2}]"
    }
  }

  test("return {name:Mercury,id:1}") {
    Get("/webapi/entities/1") ~> route ~> check {
      responseAs[String] shouldEqual "{\"name\":\"Mercury\",\"id\":1}"
    }
  }

  test("return 'entity saved'") {
    Post("/webapi/entities",Entity("Earth")) -> route -> check {
      responseAs[String] shouldEqual "entity saved"
    }
  }

  test("return 'entity deleted'") {
    Delete("/webapi/entities/2") -> route -> check {
      responseAs[String] shouldEqual "entity deleted"
    }
  }

  test("return 'entity with id 2 updated'") {
    Put("/webapi/entities",Entity("Earth",2)) -> route -> check {
      responseAs[String] shouldEqual "entity with id 2 updated"
    }
  }

  test("return status 404 NotFound after failed get") {
    Get("/webapi/entities/3") -> route -> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  test("return status 404 NotFound after failed update") {
    Put("/webapi/entities",Entity("Earth",3)) -> route -> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  test("return status 404 NotFound after failed delete") {
    Delete("/webapi/entities/3") -> route -> check {
      status shouldEqual StatusCodes.NotFound
    }
  }
}