package org.kolokolov.rest.unit

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.kolokolov.model.{Comment, Message, Customer}
import org.kolokolov.rest.{JsonSupport, RestController}
import org.kolokolov.service.{CommentService, MessageService, UserService}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.Future

/**
  * Created by Kolokolov on 11.05.2017.
  */
class RestControllerTest extends AsyncFunSuite
  with AsyncMockFactory
  with Matchers
  with ScalatestRouteTest
  with JsonSupport {

  private val firstUser = Customer("Bob Marley",1)
  private val secondUser = Customer("Tam Waits",2)

  private val firstMessage = Message("Hello, everybody!",1,1)
  private val secondMessage = Message("Happy New Year!",2,2)

  private val firstComment = Comment("Hello, everybody!",1,1,1)
  private val secondComment = Comment("Happy New Year!",2,2,2)

//  def createRoute: Route = {
//    val stubUserService = stub[UserService]
//    (stubUserService.getUserById _).when(1).returns(Future(Some(firstUser)))
//    (stubUserService.getAllUsers _).when().returns(Future(Seq(firstUser,secondUser)))
//    val stubMessageService = stub[MessageService]
//    (stubMessageService.getMessageById _).when(1).returns(Future(Some(firstMessage)))
//    (stubMessageService.getAllMessages _).when().returns(Future(Seq(firstMessage,secondMessage)))
//    val stubCommentService = stub[CommentService]
//    (stubCommentService.getCommentById _).when(1).returns(Future(Some(firstComment)))
//    (stubCommentService.getAllComments _).when().returns(Future(Seq(firstComment,secondComment)))
//    new RestController(stubUserService, stubMessageService, stubCommentService, system).rootRoute
//  }
//
//  test("should return all users") {
//    val route = createRoute
//    Get("/webapi/users") ~> route ~> check {
//      responseAs[Seq[Customer]] shouldEqual Seq(firstUser,secondUser)
//    }
//  }
//
//  test("should return firstUser") {
//    val route = createRoute
//    Get("/webapi/users/1") ~> route ~> check {
//      responseAs[Customer] shouldEqual firstUser
//    }
//  }
//
//  test("should return all messages") {
//    val route = createRoute
//    Get("/webapi/messages") ~> route ~> check {
//      responseAs[Seq[Message]] shouldEqual Seq(firstMessage,secondMessage)
//    }
//  }
//
//  test("should return firsMessage") {
//    val route = createRoute
//    Get("/webapi/messages/1") ~> route ~> check {
//      responseAs[Message] shouldEqual firstMessage
//    }
//  }
//
//  test("should return all comments") {
//    val route = createRoute
//    Get("/webapi/comments") ~> route ~> check {
//      responseAs[Seq[Comment]] shouldEqual Seq(firstComment,secondComment)
//    }
//  }
}