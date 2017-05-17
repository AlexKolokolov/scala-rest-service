package org.kolokolov.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.kolokolov.model.{Message, User}
import org.kolokolov.service.{CommentService, MessageService, UserService}
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by Kolokolov on 11.05.2017.
  */
class RestController(val userService: UserService, val messageService: MessageService, val commentService: CommentService, val system: ActorSystem) extends JsonSupport {

  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  private val logger = LoggerFactory.getLogger(this.getClass)

  val route: Route =
    pathPrefix("webapi") {
      get {
        path("users") {
          val usersFuture: Future[Seq[User]] = userService.getAllUsers
          onComplete(usersFuture) {
            case Success(users) => complete(users)
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        }
      } ~
      get {
        path("users" / IntNumber) { id =>
          val userFuture = userService.getUserById(id)
          onComplete(userFuture) {
            case Success(optionUser) => optionUser match {
              case Some(user) => complete(user)
              case None => complete(StatusCodes.NoContent, s"User with id $id not found")
            }
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        }
      } ~
      post {
        path("users") {
          entity(as[User]) {
            user =>
              val savedUser = userService.saveUser(user)
              onComplete(savedUser) {
                case Success(linesModified) => linesModified match {
                  case 1 => complete(StatusCodes.Created, s"User $user has been saved")
                  case _ => complete(StatusCodes.BadRequest)
                }
                case Failure(ex) => {
                  logger.error(ex.getStackTrace.mkString)
                  complete(StatusCodes.InternalServerError)
                }
              }
          }
        }
      } ~
      delete {
        path("users" / IntNumber) { id =>
          val deletedUser = userService.deleteUser(id)
          onComplete(deletedUser) {
            case Success(n) => n match {
              case 1 => complete(s"User with id $id deleted")
              case _ => complete(StatusCodes.NotFound, s"User with id $id was not found")
            }
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        }
      } ~
      put {
        path("users") {
          entity(as[User]) { user =>
            val updatedUser = userService.updateUser(user)
            onComplete(updatedUser) {
              case Success(n) => n match {
                case 1 => complete(StatusCodes.Accepted, s"User $user has been updated")
                case _ => complete(StatusCodes.BadRequest, s"User with id ${user.id} not found")
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      } ~
      get {
        path("users" / IntNumber / "messages") { id =>
          val messagesFuture: Future[Seq[Message]] = messageService.getMessagesByAuthorId(id)
          onComplete(messagesFuture) {
            case Success(messages) => complete(messages)
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        }
      } ~
      get {
        pathPrefix("users" / IntNumber) { userId =>
          path("messages" / IntNumber) { messageId =>
            val messageFuture: Future[Option[Message]] = messageService.getUsersMessageById(userId, messageId)
            onComplete(messageFuture) {
              case Success(optionMessage) => optionMessage match {
                case Some(message) => complete(message)
                case None => complete(StatusCodes.NoContent, s"Message with id: $messageId by user with id $userId not found")
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      }
    }
}


