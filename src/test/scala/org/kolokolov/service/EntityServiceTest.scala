package org.kolokolov.service

import org.kolokolov.model.Entity
import org.kolokolov.repo.H2Database
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Kolokolov on 5/10/17.
  */
class EntityServiceTest extends AsyncFunSuite
  with Matchers
  with BeforeAndAfterEach {

  private val entityService = new EntityService with H2Database

  private val dbTestHelper = new DBTestHelper with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  test("getEntityById(1) should return User(Mercury, 1)") {
    entityService.getEntityById(1).map {
      result => result shouldEqual Some(Entity("Mercury",1))
    }
  }

  test("getAllEntities should return Seq(Entity(Mercury,1), Entity(Venus, 2))") {
    entityService.getAllEntities.map {
      result => result shouldEqual Seq(Entity("Mercury", 1), Entity("Venus", 2))
    }
  }

  test("getEntityById(1) should return None after deleteEntity(1)") {
    entityService.deleteEntity(1).flatMap {
      delRes => {
        delRes shouldEqual 1
        entityService.getEntityById(1).map {
          result => result shouldEqual None
        }
      }
    }
  }

  test("getAllUsers.length should return 3") {
    entityService.saveEntity(Entity("Earth")).flatMap(
      addRes => {
        addRes shouldEqual 1
        entityService.getAllEntities.map {
          result => result.length shouldEqual 3
        }
      }
    )
  }

  test("getEntityById(2) should return Entity(Earth,2) after updateEntity(Entity(Earth,2))") {
    entityService.updateEntity(Entity("Earth",2)).flatMap(
      addRes => {
        addRes shouldEqual 1
        entityService.getEntityById(2).map {
          result => result shouldEqual Some(Entity("Earth", 2))
        }
      }
    )
  }
}