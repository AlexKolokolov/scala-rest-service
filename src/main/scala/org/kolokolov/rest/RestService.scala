package org.kolokolov.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import org.kolokolov.model.Entity
import org.kolokolov.repo.H2Database
import org.kolokolov.service.EntityService
import spray.json._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val entityFormat: RootJsonFormat[Entity] = jsonFormat2(Entity)
}

/**
  * Created by Kolokolov on 11.05.2017.
  */
object RestService extends JsonSupport {

  implicit val system = ActorSystem("rest-service-actor-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val entityService = new EntityService with H2Database
  val dbHelper = new DBHelper with H2Database

  val route: Route =
    pathPrefix("webapi") {
      get {
        path("entities") {
          val entitiesFuture: Future[Seq[Entity]] = entityService.getAllEntities
          onSuccess(entitiesFuture) {
            entities => complete(entities)
          }
        }
      } ~
        get {
          path("entities" / IntNumber) { id =>
            val entityFuture = entityService.getEntityById(id)
            onSuccess(entityFuture) {
              case Some(entity) => complete(entity)
              case None => complete(StatusCodes.NotFound)
            }
          }
        } ~
        post {
          path("entities") {
            entity(as[Entity]) {
              entity =>
                val savedEntity = entityService.saveEntity(entity)
                onComplete(savedEntity) {
                  case Success(n) => n match {
                    case 1 => complete("entity saved")
                    case _ => complete(StatusCodes.BadRequest)
                  }
                  case Failure(ex) => complete(StatusCodes.BadRequest)
                }
            }
          }
        } ~
        delete {
          path("entities" / IntNumber) { id =>
            val deletedEntity = entityService.deleteEntity(id)
            onComplete(deletedEntity) {
              case Success(n) => n match {
                case 1 => complete("entity deleted")
                case _ => complete(StatusCodes.NotFound)
              }
              case Failure(ex) => complete(StatusCodes.NotFound)
            }
          }
        } ~
        put {
          path("entities") {
            entity(as[Entity]) { entity =>
              val updatedEntity = entityService.updateEntity(entity)
              onComplete(updatedEntity) {
                case Success(n) => n match {
                  case 1 => complete(s"entity with id ${entity.id} updated")
                  case _ => complete(StatusCodes.BadRequest)
                }
                case Failure(ex) => complete(StatusCodes.BadRequest)
              }
            }
          }
        }
    }


  def main(args: Array[String]) {

    Await.result(dbHelper.setupDB, Duration.Inf)

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
