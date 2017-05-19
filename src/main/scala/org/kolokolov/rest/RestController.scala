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

  implicit val executionContext = system.dispatcher

  private val logger = LoggerFactory.getLogger(this.getClass)

  val rootRoute = Route {
    pathPrefix("webapi") {
      usersRoute ~ messagesRoute ~ commentsRoute
    }
  }

  private val usersRoute: Route =
    pathPrefix("users") {
      pathEndOrSingleSlash {
        get {
          // GET /webapi/users - Get all users
          val usersFuture: Future[Seq[User]] = userService.getAllUsers
          onComplete(usersFuture) {
            case Success(users) => complete(users)
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        } ~
        post {
          // POST /webapi/users - Add new user
          entity(as[User]) { user =>
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
        } ~
        put {
          // PUT /webapi/users - Update user
          entity(as[User]) { user =>
            val updatedUser = userService.updateUser(user)
            onComplete(updatedUser) {
              case Success(n) => n match {
                case 1 => complete(StatusCodes.ResetContent, s"User with ID: ${user.id} has been updated")
                case _ => complete(StatusCodes.BadRequest, s"User with ID: ${user.id} was not found")
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      } ~
      pathPrefix(IntNumber) { userId =>
        pathEndOrSingleSlash {
          get {
            // GET /webapi/users/1 - Get user by ID
            val userFuture: Future[Option[User]] = userService.getUserById(userId)
            onComplete(userFuture) {
              case Success(optionUser) => optionUser match {
                case Some(user) => complete(user)
                case None => complete(StatusCodes.NotFound, s"User with ID: $userId was not found")
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          } ~
          delete {
            // DELETE /webapi/users/1 - Delete user by ID
            val deletedUser = userService.deleteUser(userId)
            onComplete(deletedUser) {
              case Success(n) => n match {
                case 1 => complete(StatusCodes.Accepted, s"User with ID: $userId has been deleted")
                case _ => complete(StatusCodes.NotFound, s"User with ID: $userId was not found")
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        pathPrefix("messages") {
          pathEndOrSingleSlash {
            get {
              // GET /webapi/users/1/messages - Get all messages of a particular user
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
          pathPrefix(IntNumber) { messageId =>
            pathEndOrSingleSlash {
              get {
                // GET /webapi/users/1/messages/1 - Get message by ID of a particular user
                val messageFuture: Future[Option[Message]] = messageService.getUsersMessageById(userId, messageId)
                onComplete(messageFuture) {
                  case Success(optionMessage) => optionMessage match {
                    case Some(message) => complete(message)
                    case None => complete(StatusCodes.NotFound, s"Message with ID: $messageId by user with ID: $userId not found")
                  }
                  case Failure(ex) => {
                    logger.error(ex.getStackTrace.mkString)
                    complete(StatusCodes.InternalServerError)
                  }
                }
              }
            } ~
            pathPrefix("comments"){
              pathEndOrSingleSlash {
                get {
                  // GET /webapi/users/1/messages/1/comments - Get all comments to particular message of particular user
                  val commentsFuture = commentService.getCommentsByAuthorsMessageId(userId,messageId)
                  onComplete(commentsFuture) {
                    case Success(comments) => complete(comments)
                    case Failure(ex) => {
                      logger.error(ex.getStackTrace.mkString)
                      complete(StatusCodes.InternalServerError)
                    }
                  }
                }
              }
            }
          }
        } ~
        pathPrefix("comments") {
          pathEndOrSingleSlash {
            get {
              // GET /webapi/users/1/comments - Get all comments of particular user
              val commentsFuture = commentService.getCommentsByAuthorId(userId)
              onComplete(commentsFuture) {
                case Success(comments) => complete(comments)
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

  private val messagesRoute: Route =
    pathPrefix("messages") {
      pathEndOrSingleSlash{
        get {
          // GET /webapi/messages - Get all messages
          val messagesFuture: Future[Seq[Message]] = messageService.getAllMessages
          onComplete(messagesFuture) {
            case Success(messages) => complete(messages)
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        } ~
        post {
          //POST /webapi/messages - Add new message
          entity(as[Message]) { message =>
            val savedMessage: Future[Int] = messageService.saveMessage(message)
            onComplete(savedMessage) {
              case Success(linesAdded) => linesAdded match {
                case 1 => complete(StatusCodes.Created, s"Message $message has been saved")
                case _ => complete(StatusCodes.BadRequest)
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.BadRequest)
              }
            }
          }
        } ~
        put {
          // PUT /webapi/messages - Edit message
          entity(as[Message]) { message =>
            val updatedMessage = messageService.updateUsersMessage(message)
            onComplete(updatedMessage) {
              case Success(linesAffected) => linesAffected match {
                case 1 => complete(StatusCodes.ResetContent, s"Message $message has been updated")
                case _ => complete(StatusCodes.BadRequest, s"Message with ID: ${message.id} by user with ID : ${message.authorId} was not found")
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      } ~
      pathPrefix(IntNumber) { messageId =>
        pathEndOrSingleSlash {
          get {
            // GET /webapi/messages/1 - Get message by ID
            val messageFuture: Future[Option[Message]] = messageService.getMessageById(messageId)
            onComplete(messageFuture) {
              case Success(optionMessage) => optionMessage match {
                case Some(message) => complete(message)
                case None => complete(StatusCodes.NotFound, s"Message with ID: $messageId was not found")
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          } ~
          delete {
            // DELETE /webapi/messages/1 - Delete message by ID
            val deletedMessage = messageService.deleteMessage(messageId)
            onComplete(deletedMessage) {
              case Success(n) => n match {
                case 1 => complete(StatusCodes.Accepted, s"Message with ID: $messageId has been deleted")
                case _ => complete(StatusCodes.NotFound, s"Message with ID: $messageId was not found")
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        pathPrefix("comments"){
          pathEndOrSingleSlash {
            get {
              // GET /webapi/messages/1/comments - Get all comments to particular message
              val commentsFuture = commentService.getCommentsByMessageId(messageId)
              onComplete(commentsFuture) {
                case Success(comments) => complete(comments)
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

  private val commentsRoute =
    pathPrefix("comments") {
      pathEndOrSingleSlash {
        get {
          // GET /webapi/comments - Get all comments
          val commentsFuture: Future[Seq[Comment]] = commentService.getAllComments
          onComplete(commentsFuture) {
            case Success(comments) => complete(comments)
            case Failure(ex) => {
              logger.error(ex.getStackTrace.mkString)
              complete(StatusCodes.InternalServerError)
            }
          }
        } ~
        post {
          // POST /webapi/comments - Add new comment
          entity(as[Comment]) { comment =>
            val savedComment = commentService.saveComment(comment)
            onComplete(savedComment) {
              case Success(linesAdded) => linesAdded match {
                case 1 => complete(StatusCodes.Created, s"Comment $comment has been saved")
                case _ => complete(StatusCodes.BadRequest)
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.BadRequest)
              }
            }
          }
        } ~
        put {
          // PUT /webapi/comments - Update comment
          entity(as[Comment]) { comment =>
            val savedComment = commentService.updateUsersCommentToMessage(comment)
            onComplete(savedComment) {
              case Success(linesAdded) => linesAdded match {
                case 1 => complete(StatusCodes.ResetContent, s"Comment $comment has been updated")
                case _ => complete(StatusCodes.BadRequest,
                  s"Comment with ID ${comment.id} by user with ID: ${comment.authorId} to message with ID: ${comment.messageId} was not found")
              }
              case Failure(ex) => {
                logger.error(ex.getStackTrace.mkString)
                complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      } ~
      pathPrefix(IntNumber) { commentId =>
        pathEndOrSingleSlash {
          delete {
            // DELETE /webapi/comments/1 - Delete comment by ID
            val deletedComment: Future[Int] = commentService.deleteComment(commentId)
            onComplete(deletedComment) {
              case Success(n) => n match {
                case 1 => complete(StatusCodes.Accepted, s"Comment with ID: $commentId has been deleted")
                case _ => complete(StatusCodes.NotFound, s"Comment with ID: $commentId was not found")
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


