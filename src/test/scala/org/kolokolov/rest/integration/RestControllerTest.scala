package org.kolokolov.rest.integration

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.kolokolov.repo.H2Database
import org.kolokolov.rest.{JsonSupport, RestController}
import org.kolokolov.service.{CommentService, MessageService, TestDBCreator, UserService}
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import akka.http.scaladsl.model._
import org.kolokolov.model.{Comment, Message, User}
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Kolokolov on 11.05.2017.
  */
class RestControllerTest extends AsyncFunSuite
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfterEach
  with JsonSupport{

  private val route = new RestController(new UserService(H2Profile), new MessageService(H2Profile), new CommentService(H2Profile), ActorSystem("test-actor-system")).rootRoute

  val dbTestHelper = new TestDBCreator with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  test("should return all users") {
    Get("/webapi/users") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[User]] shouldEqual Seq(User("Bob Marley",1), User("Tom Waits",2), User("Guy Pearce",3))
    }
  }

  test("should return {name:Bob Marley,id:1}") {
    Get("/webapi/users/1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[User] shouldEqual User("Bob Marley",1)
    }
  }

  test("should return 'User User(Ron Perlman,0) has been saved'") {
    Post("/webapi/users",User("Ron Perlman")) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[String] shouldEqual "User User(Ron Perlman,0) has been saved"
    }
  }

  test("should return 'User with ID: 1 has been deleted'") {
    Delete("/webapi/users/1") ~> route ~> check {
      status shouldEqual StatusCodes.Accepted
      responseAs[String] shouldEqual "User with ID: 1 has been deleted"
    }
  }

  test("return 'User with ID: 2 has been updated'") {
    Put("/webapi/users",User("Ron Perlman",2)) ~> route ~> check {
      status shouldEqual StatusCodes.ResetContent
      responseAs[String] shouldEqual "User with ID: 2 has been updated"
    }
  }

  test("should return 'User with ID: 4 was not found' after illegal get") {
    Get("/webapi/users/4") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "User with ID: 4 was not found"
    }
  }

  test("should return 'User with ID: 4 was not found' after illegal put") {
    Put("/webapi/users",User("Ron Perlman",4)) ~> route ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[String] shouldEqual "User with ID: 4 was not found"
    }
  }

  test("should return 'User with ID: 4 was not found' after illegal delete") {
    Delete("/webapi/users/4") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "User with ID: 4 was not found"
    }
  }

  test("should return all messages of 1 user") {
    Get("/webapi/users/1/messages") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[Message]] shouldEqual Seq(Message("Rock sucks!",1,1), Message("Good morning to everyone!",1,2))
    }
  }

  test("should return 1 message of 1 user") {
    Get("/webapi/users/1/messages/1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Message] shouldEqual Message("Rock sucks!",1,1)
    }
  }

  test("should return all comments to 3 message of 2 user") {
    Get("/webapi/users/2/messages/3/comments") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[Comment]] shouldEqual Seq(Comment("Great! I love it!",3,3,2))
    }
  }

  test("should return empty array of comments to 1 message of 2 user") {
    Get("/webapi/users/2/messages/1/comments") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[Comment]] shouldEqual Seq.empty
    }
  }

  test("should return array of 4 elements") {
    Get("/webapi/messages") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[Message]].length shouldEqual 4
    }
  }

  test("should return Message(Happy New Year!,3,4)") {
    Get("/webapi/messages/4") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Message] shouldEqual Message("Happy New Year!",3,4)
    }
  }

  test("should return array of 2 comments)") {
    Get("/webapi/messages/4/comments") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[Comment]].length shouldEqual 2
    }
  }

  test("should return 'Message Message(Great weather!,2,0) has been added'") {
    Post("/webapi/messages", Message("Great weather!",2)) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[String] shouldEqual "Message Message(Great weather!,2,0) has been saved"
    }
  }

  test("should return sratus BadRequest") {
    Post("/webapi/messages", Message("Great weather!",4)) ~> route ~> check {
      status shouldEqual StatusCodes.BadRequest
    }
  }

  test("should return 'Message Message(Great weather!,3,4) has been updated'") {
    Put("/webapi/messages", Message("Great weather!",3,4)) ~> route ~> check {
      status shouldEqual StatusCodes.ResetContent
      responseAs[String] shouldEqual "Message Message(Great weather!,3,4) has been updated"
    }
  }

  test("should return 'Message with ID: 5 by user with ID : 3 was not found'") {
    Put("/webapi/messages", Message("Great weather!",3,5)) ~> route ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[String] shouldEqual "Message with ID: 5 by user with ID : 3 was not found"
    }
  }

  test("should return 'Message with ID: 5 was not found'") {
    Delete("/webapi/messages/5") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Message with ID: 5 was not found"
    }
  }

  test("should return 'Message with ID: 1 has been deleted'") {
    Delete("/webapi/messages/1") ~> route ~> check {
      status shouldEqual StatusCodes.Accepted
      responseAs[String] shouldEqual "Message with ID: 1 has been deleted"
    }
  }

  test("should return array of 4 comments)") {
    Get("/webapi/comments") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Seq[Comment]].length shouldEqual 4
    }
  }

  test("should return 'Comment(Morning,2,3,0) has been added'") {
    Post("/webapi/comments", Comment("Morning",2,3)) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[String] shouldEqual "Comment Comment(Morning,2,3,0) has been saved"
    }
  }

  test("should return status BadRequest") {
    Post("/webapi/comments", Comment("Morning",2,5)) ~> route ~> check {
      status shouldEqual StatusCodes.BadRequest
    }
  }

  test("should return 'Comment Comment(Morning,1,2,1) has been updated'") {
    Put("/webapi/comments", Comment("Morning",1,2,1)) ~> route ~> check {
      status shouldEqual StatusCodes.ResetContent
      responseAs[String] shouldEqual "Comment Comment(Morning,1,2,1) has been updated"
    }
  }

  test("should return 'Comment with ID 2 by user with ID: 5 to message with ID: 2 was not found'") {
    Put("/webapi/comments", Comment("Morning",2,5,2)) ~> route ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[String] shouldEqual "Comment with ID 2 by user with ID: 5 to message with ID: 2 was not found"
    }
  }

  test("should return 'Comment with ID: 1 has been deleted'") {
    Delete("/webapi/comments/1") ~> route ~> check {
      status shouldEqual StatusCodes.Accepted
      responseAs[String] shouldEqual "Comment with ID: 1 has been deleted"
    }
  }

  test("should return 'Comment with ID: 5 was not found'") {
    Delete("/webapi/comments/5") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Comment with ID: 5 was not found"
    }
  }
}