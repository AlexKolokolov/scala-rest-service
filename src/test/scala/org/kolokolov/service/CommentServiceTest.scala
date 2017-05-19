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

  test("getCommentById(4) should return Some(Comment(Thank you, man!,4,1,4))") {
    getCommentById(4).map { result =>
      result shouldEqual Some(Comment("Thank you, man!",4,1,4))
    }
  }

  test("getCommentById(5) should return None") {
    getCommentById(5).map { result =>
      result shouldEqual None
    }
  }

  test("getCommentById(5) should return Some(Message(Great news!,3,3,5)) after saveComment(Comment(Great news!,3,3))") {
    saveComment(Comment("Great news!",3,3)).flatMap { saveResult =>
      saveResult shouldEqual 1
      getCommentById(5).map { result =>
        result shouldEqual Some(Comment("Great news!",3,3,5))
      }
    }
  }

  test("getComment(1) should return None after deleteComment(1)") {
    deleteComment(1).flatMap { deleteResult =>
      deleteResult shouldEqual 1
      getCommentById(1).map { result =>
        result shouldEqual None
      }
    }
  }

  test("deleteComment(5) should return 0") {
    deleteComment(5).map { deleteResult =>
      deleteResult shouldEqual 0
    }
  }

  test("deleteUsersComment(1,1) should return 0") {
    deleteUsersComment(1,1).map { deleteResult =>
      deleteResult shouldEqual 0
    }
  }

  test("getCommentsByAuthorId(1) should return empty Seq[Comment] after deleteUsersComment(1,4)") {
    deleteUsersComment(1,4).flatMap { deleteResult =>
      deleteResult shouldEqual 1
      getCommentsByAuthorId(1).map { result =>
        result shouldEqual Seq.empty
      }
    }
  }

  test("getCommentById(1) should return Some(Comment(Shut up!,1,2,1)) after updateUsersCommentToMessage(Comment(Shut up!,1,2,1)))") {
    updateUsersCommentToMessage(Comment("Shut up!",1,2,1)).flatMap { updateResult =>
      updateResult shouldEqual 1
      getCommentById(1).map { result =>
        result shouldEqual Some(Comment("Shut up!",1,2,1))
      }
    }
  }

  test("updateUsersCommentToMessage(Comment(Shut up!,1,1,1))) should return 0") {
    updateUsersCommentToMessage(Comment("Shut up!",1,1,1)).map { updateResult =>
      updateResult shouldEqual 0
    }
  }

  test("getCommentsByAuthorId(1) should return Seq(Comment(Thank you, man!,4,1,4)))") {
    getCommentsByAuthorId(1).map { result =>
      result shouldEqual Seq(Comment("Thank you, man!",4,1,4))
    }
  }

  test("getCommentsByAuthorsMessageId(2,3) should return Seq(Comment(Great! I love it!,3,3,2))") {
    getCommentsByAuthorsMessageId(2,3).map { result =>
      result shouldEqual Seq(Comment("Great! I love it!",3,3,2))
    }
  }

  test("getCommentsByAuthorsMessageId(3,3) should return empty Seq[Comment]") {
    getCommentsByAuthorsMessageId(3,3).map { result =>
      result shouldEqual Seq.empty
    }
  }
}