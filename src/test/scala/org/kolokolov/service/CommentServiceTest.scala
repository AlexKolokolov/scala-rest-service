package org.kolokolov.service

import org.kolokolov.model.Comment
import org.kolokolov.repo.H2Database
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Kolokolov on 5/10/17.
  */
class CommentServiceTest extends AsyncFunSuite
  with Matchers
  with BeforeAndAfterEach {

  private val commentService = new CommentService(H2Profile)

  import commentService._

  private val dbTestHelper = new TestDBCreator with H2Database

  override def beforeEach: Unit = {
    Await.result(dbTestHelper.setupDB, Duration.Inf)
  }

  override def afterEach: Unit = {
    Await.result(dbTestHelper.cleanDB, Duration.Inf)
  }

  test("getAllComments should return Seq[Message] with length 4") {
    getAllComments.map { result =>
      result.length shouldEqual 4
    }
  }

  test("getCommentById(4) should return Some(Comment(Thank you, man!,3,1,4))") {
    getCommentById(4).map { result =>
      result shouldEqual Some(Comment("Thank you, man!",3,1,4))
    }
  }

  test("getCommentById(5) should return None") {
    getCommentById(5).map { result =>
      result shouldEqual None
    }
  }

//  test("getMessageById(1) should return None after deleteMessage(1)") {
//    deleteMessage(1).flatMap { delResult =>
//        delResult shouldEqual 1
//        getMessageById(1).map { result =>
//          result shouldEqual None
//      }
//    }
//  }
//
//  test("deleteMessage(5) should return 0)") {
//    deleteMessage(5).map {
//      result => result shouldEqual 0
//    }
//  }
//
//  test("getMessagesByAuthorId(1) should return Seq[Message] with length 2") {
//    getMessagesByAuthorId(1).map { result =>
//      result.length shouldEqual 2
//    }
//  }
//
//  test("getMessagesByAuthorId(4) should return empty Seq[Message]") {
//    getMessagesByAuthorId(4).map { result =>
//      result shouldEqual Seq.empty
//    }
//  }
//
//  test("getMessagesById(4) should return Seq[Message] of length 2 after saveMessage(Message(Never say never!, 3))") {
//    saveMessage(Message("Never say never!", 3)).flatMap { saveResult =>
//      saveResult shouldEqual 1
//      getMessagesByAuthorId(3).map { result =>
//        result.length shouldEqual 2
//      }
//    }
//  }
//
//  test("getMessagesById(4) should return Seq(Message(Never say never!,3,4)) after updateUsersMessage(Message(Never say never!,3,4))") {
//    updateUsersMessage(Message("Never say never!", 3, 4)).flatMap { saveResult =>
//      saveResult shouldEqual 1
//      getMessagesByAuthorId(3).map { result =>
//        result shouldEqual Seq(Message("Never say never!", 3, 4))
//      }
//    }
//  }
//
//  test("updateUsersMessage(Message(Never say never!,2,4)) should equal 0") {
//    updateUsersMessage(Message("Never say never!",2,4)).map { saveResult =>
//      saveResult shouldEqual 0
//    }
//  }
//
//  test("getUsersMessageById(1,1) should return Some(Message(Rock sucks!,1,1))") {
//    getUsersMessageById(1,1).map { result =>
//      result shouldEqual Some(Message("Rock sucks!",1,1))
//    }
//  }
//
//  test("getUsersMessageById(1,4) should return None") {
//    getUsersMessageById(1,4).map { result =>
//      result shouldEqual None
//    }
//  }
//
//  test("getMessagesByAuthorId(3) should return empty Seq[Message] after deleteUsersMessage(3,4)") {
//    deleteUsersMessage(3,4).flatMap { delResult =>
//      delResult shouldEqual 1
//      getMessagesByAuthorId(3).map { result =>
//        result shouldEqual Seq.empty
//      }
//    }
//  }
//
//  test("deleteUsersMessage(1,4) should return 0") {
//    deleteUsersMessage(1,4).map { delResult =>
//      delResult shouldEqual 0
//    }
//  }
}