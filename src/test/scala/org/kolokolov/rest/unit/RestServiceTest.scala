package org.kolokolov.rest.unit

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.kolokolov.model.Entity
import org.kolokolov.rest.{JsonSupport, RestController}
import org.kolokolov.service.EntityService
import org.scalamock.scalatest.MockFactory
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.Future

/**
  * Created by Kolokolov on 11.05.2017.
  */
class RestServiceTest extends AsyncFunSuite
  with Matchers
  with ScalatestRouteTest
  with JsonSupport
  with MockFactory {

  private val firstEntity = Entity("Mercury",1)
  private val secondEntity = Entity("Venus",2)

  private val stubEntityService = stub[EntityService]
  (stubEntityService.getEntityById _).when(1).returns(Future(Some(firstEntity)))
  (stubEntityService.getAllEntities _).when().returns(Future(Seq(firstEntity,secondEntity)))

  private val route = new RestController(stubEntityService, ActorSystem("test-actor-system")).route

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
}