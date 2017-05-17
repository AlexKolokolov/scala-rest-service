package org.kolokolov.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.kolokolov.model.{Comment, Message, User}
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
          path("users" / IntNumber) { userId =>
            val userFuture = userService.getUserById(userId)
            onComplete(userFuture) {
              case Success(optionUser) => optionUser match {
                case Some(user) => complete(user)
                case None => complete(StatusCodes.NoContent, s"User with id $userId not found")
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        delete {
          path("users" / IntNumber) { userId =>
            val deletedUser = userService.deleteUser(userId)
            onComplete(deletedUser) {
              case Success(n) => n match {
                case 1 => complete(s"User with id $userId deleted")
                case _ => complete(StatusCodes.NotFound, s"User with id $userId was not found")
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        pathPrefix("users" / IntNumber) { userId =>
          get {
            path("messages") {
              val messagesFuture: Future[Seq[Message]] = messageService.getMessagesByAuthorId(userId)
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
            } ~
            post {
              path("messages") {
                entity(as[Message]) {
                  message =>
                    message.authorId match {
                      case `userId` => {
                        val savedUser = messageService.saveMessage(message)
                        onComplete(savedUser) {
                          case Success(linesModified) => linesModified match {
                            case 1 => complete(StatusCodes.Created, s"Message $message has been saved")
                            case _ => complete(StatusCodes.BadRequest)
                          }
                          case Failure(ex) => {
                            logger.error(ex.getStackTrace.mkString)
                            complete(StatusCodes.InternalServerError)
                          }
                        }
                      }
                      case _ => complete(StatusCodes.BadRequest, s"Wrong author ID : ${message.authorId}")
                    }
                }
              }
            } ~
            delete {
              path("messages" / IntNumber) { messageId =>
                val deletedMessage = messageService.deleteUsersMessage(userId, messageId)
                onComplete(deletedMessage) {
                  case Success(n) => n match {
                    case 1 => complete(s"Message with ID : $messageId has been deleted")
                    case _ => complete(StatusCodes.NotFound, s"Message with ID: $messageId by user with ID : $userId was not found")
                  }
                  case Failure(ex) => {
                    logger.error(ex.getStackTrace.mkString)
                    complete(StatusCodes.InternalServerError)
                  }
                }
              }
            } ~
            put {
              path("messages") {
                entity(as[Message]) { message =>
                  message.authorId match {
                    case `userId` => {
                      val updatedMessage = messageService.updateUsersMessage(message)
                      onComplete(updatedMessage) {
                        case Success(linesAffected) => linesAffected match {
                          case 1 => complete(StatusCodes.Accepted, s"Message $message has been updated")
                          case _ => complete(StatusCodes.BadRequest, s"Message with ID: ${message.id} by user with ID : $userId was not found")
                        }
                        case Failure(ex) => {
                          logger.error(ex.getStackTrace.mkString)
                          complete(StatusCodes.InternalServerError)
                        }
                      }
                    }
                    case _ => complete(StatusCodes.BadRequest, s"Wrong author ID : ${message.authorId}")
                  }
                }
              }
            } ~
            get {
              path("comments") {
                val commentsFuture = commentService.getCommentsByAuthorId(userId)
                onComplete(commentsFuture) {
                  case Success(comments) => complete(comments)
                  case Failure(ex) => {
                    logger.error(ex.getStackTrace.mkString)
                    complete(StatusCodes.InternalServerError)
                  }
                }
              }
            } ~
            delete {
              path("comments" / IntNumber) { commentId =>
                val deletedComment: Future[Int] = commentService.deleteUsersComment(userId, commentId)
                onComplete(deletedComment) {
                  case Success(n) => n match {
                    case 1 => complete(s"Comment with ID : $commentId has been deleted")
                    case _ => complete(StatusCodes.NotFound, s"Comment with ID : $commentId by user with ID: $userId")
                  }
                  case Failure(ex) => {
                    logger.error(ex.getStackTrace.mkString)
                    complete(StatusCodes.InternalServerError)
                  }
                }
              }
            } ~
            pathPrefix("messages" / IntNumber) { messageId =>
              get {
                path("comments") {
                  val commentsFuture = commentService.getCommentsByMessageId(messageId)
                  onComplete(commentsFuture) {
                    case Success(comments) => complete(comments)
                    case Failure(ex) => {
                      logger.error(ex.getStackTrace.mkString)
                      complete(StatusCodes.InternalServerError)
                    }
                  }
                }
              } ~
              delete {
                path("comments" / IntNumber) { commentId =>
                  val deletedComment: Future[Int] = commentService.deleteUsersCommentToMessage(userId,messageId,commentId)
                  onComplete(deletedComment) {
                    case Success(n) => n match {
                      case 1 => complete(s"Comment with ID : $commentId has been deleted")
                      case _ => complete(StatusCodes.NotFound, s"Comment with ID : $commentId by user with ID: $userId to message with ID : $messageId was not found")
                    }
                    case Failure(ex) => {
                      logger.error(ex.getStackTrace.mkString)
                      complete(StatusCodes.InternalServerError)
                    }
                  }
                }
              }
            }
        } ~
        get {
          path("messages") {
            val messagesFuture: Future[Seq[Message]] = messageService.getAllMessages
            onComplete(messagesFuture) {
              case Success(messages) => complete(messages)
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        delete {
          path("messages" / IntNumber) { messageId =>
            val deletedMessage: Future[Int] = messageService.deleteMessage(messageId)
            onComplete(deletedMessage) {
              case Success(n) => n match {
                case 1 => complete(s"Message with id $messageId has been deleted")
                case _ => complete(StatusCodes.NotFound, s"Message with id $messageId was not found")
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        get {
          path("comments") {
            val commentsFuture: Future[Seq[Comment]] = commentService.getAllComments
            onComplete(commentsFuture) {
              case Success(comments) => complete(comments)
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        delete {
          path("comments" / IntNumber) { commentId =>
            val deletedComment: Future[Int] = commentService.deleteComment(commentId)
            onComplete(deletedComment) {
              case Success(n) => n match {
                case 1 => complete(s"Comment with id $commentId has been deleted")
                case _ => complete(StatusCodes.NotFound, s"Comment with id $commentId was not found")
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


