package org.kolokolov.rest.integration

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.kolokolov.model.Entity
import org.kolokolov.repo.H2Database
import org.kolokolov.rest.{JsonSupport, RestService}
import org.kolokolov.service.DBTestHelper
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import akka.http.scaladsl.model._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Alexey Kolokolov on 06.04.2017.
  */
class RestServiceTest extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfterEach
  with JsonSupport{

  private val route = RestService.route

  val dbTestHelper = new DBTestHelper with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  "Rest service" should {

    "return all entities" in {
      Get("/webapi/entities") ~> route ~> check {
        responseAs[String] shouldEqual "[{\"name\":\"Mercury\",\"id\":1},{\"name\":\"Venus\",\"id\":2}]"
      }
    }

    "return {name:Mercury,id:1}" in {
      Get("/webapi/entities/1") ~> route ~> check {
        responseAs[String] shouldEqual "{\"name\":\"Mercury\",\"id\":1}"
      }
    }

    "return 'entity saved'" in {
      Post("/webapi/entities",Entity("Earth")) -> route -> check {
        responseAs[String] shouldEqual "entity saved"
      }
    }

    "return 'entity deleted'" in {
      Delete("/webapi/entities/2") -> route -> check {
        responseAs[String] shouldEqual "entity deleted"
      }
    }

    "return 'entity with id 2 updated'" in {
      Put("/webapi/entities",Entity("Earth",2)) -> route -> check {
        responseAs[String] shouldEqual "entity with id 2 updated"
      }
    }

    "return status 404 NotFound after failed get" in {
      Get("/webapi/entities/3") -> route -> check {
        status shouldEqual StatusCodes.NotFound
      }
    }

    "return status 404 NotFound after failed update" in {
      Put("/webapi/entities",Entity("Earth",3)) -> route -> check {
        status shouldEqual StatusCodes.NotFound
      }
    }

    "return status 404 NotFound after failed delete" in {
      Delete("/webapi/entities/3") -> route -> check {
        status shouldEqual StatusCodes.NotFound
      }
    }
  }
}