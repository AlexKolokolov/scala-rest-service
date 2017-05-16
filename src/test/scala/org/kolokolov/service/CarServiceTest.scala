package org.kolokolov.service

import org.kolokolov.model.Car
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Kolokolov on 5/10/17.
  */
class CarServiceTest extends AsyncFunSuite
  with Matchers
  with BeforeAndAfterEach {

  private val entityService = new CarService(H2Profile)

  private val dbTestHelper = new TestDBCreator

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  test("getAllEntities should return Seq(Car(Toyota, Camry, 1, 1), Car(Toyota, Prius, 2, 2))") {
    entityService.getAllCars.map {
      result => result shouldEqual Seq(Car("Toyota", "Camry", 1,1), Car("Toyota", "Prius", 2,2))
    }
  }

}