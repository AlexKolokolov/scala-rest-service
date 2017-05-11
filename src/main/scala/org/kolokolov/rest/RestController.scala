package org.kolokolov.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.kolokolov.model.Entity
import org.kolokolov.service.EntityService

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by Kolokolov on 11.05.2017.
  */
class RestController(val entityService: EntityService, val system: ActorSystem) extends JsonSupport {

  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val route: Route =
    pathPrefix("webapi") {
      get {
        path("entities") {
          val entitiesFuture: Future[Seq[Entity]] = entityService.getAllEntities
          onComplete(entitiesFuture) {
            case Success(entities) => complete(entities)
            case Failure(ex) => complete(StatusCodes.InternalServerError)
          }
        }
      } ~
      get {
        path("entities" / IntNumber) { id =>
          val entityFuture = entityService.getEntityById(id)
          onComplete(entityFuture) {
            case Success(optionEntity) => optionEntity match {
              case Some(entity) => complete(entity)
              case None => complete(StatusCodes.NoContent, s"entity with id $id not found")
            }
            case Failure(ex) => complete(StatusCodes.InternalServerError)
          }

        }
      } ~
      post {
        path("entities") {
          entity(as[Entity]) {
            entity =>
              val savedEntity = entityService.saveEntity(entity)
              onComplete(savedEntity) {
                case Success(linesModified) => linesModified match {
                  case 1 => complete(StatusCodes.Created, "entity saved")
                  case _ => complete(StatusCodes.BadRequest)
                }
                case Failure(ex) => complete(StatusCodes.InternalServerError)
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
              case _ => complete(StatusCodes.NotFound, s"entity with id $id not found")
            }
            case Failure(ex) => complete(StatusCodes.InternalServerError)
          }
        }
      } ~
      put {
        path("entities") {
          entity(as[Entity]) { entity =>
            val updatedEntity = entityService.updateEntity(entity)
            onComplete(updatedEntity) {
              case Success(n) => n match {
                case 1 => complete(StatusCodes.Accepted, s"entity with id ${entity.id} updated")
                case _ => complete(StatusCodes.BadRequest, s"entity with id ${entity.id} not found")
              }
              case Failure(ex) => complete(StatusCodes.InternalServerError)
            }
          }
        }
      }
    }
}


