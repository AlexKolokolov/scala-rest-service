package org.kolokolov.repo

import org.kolokolov.model.{Comment, Message}

import scala.concurrent.Future

/**
  * Created by Kolokolov on 16.05.2017.
  */
trait CommentCRUDModule extends AbstractCRUDModule {

  self: UserCRUDModule with MessageCRUDModule with DatabaseProfile =>

  import profile.api._

  class CommentTable(tag: Tag) extends Table[Comment](tag, "comment_table") with IdentifiableTable[Comment] {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def text = column[String]("text")
    def messageId = column[Int]("message_id")
    def authorId = column[Int]("author_id")
    def message = foreignKey("message", messageId, MessageCRUD.dataTable)(_.id,
      onDelete = ForeignKeyAction.Cascade, onUpdate = ForeignKeyAction.Restrict)
    def author = foreignKey("comment_author", authorId, MessageCRUD.dataTable)(_.id,
      onDelete = ForeignKeyAction.Cascade, onUpdate = ForeignKeyAction.Restrict)
    def * = (text, messageId, authorId, id) <> (Comment.tupled, Comment.unapply)
  }

  object CommentCRUD extends AbstractCRUD[Comment, CommentTable] {
    lazy val dataTable: TableQuery[CommentTable] = TableQuery[CommentTable]

    def getCommentsByAuthorId(authorId: Int): Future[Seq[Comment]] = {
      val getCommentsByAuthorIdAction = dataTable.filter(_.authorId === authorId).result
      database.run(getCommentsByAuthorIdAction)
    }

    def getCommentsByMessageId(messageId: Int): Future[Seq[Comment]] = {
      val getCommentsByMessageIdAction = dataTable.filter(_.messageId === messageId).result
      database.run(getCommentsByMessageIdAction)
    }

    def getCommentsByAuthorsMessageId(authorId: Int, messageId: Int): Future[Seq[Comment]] = {
      val getCommentsByAuthorsMessageIdAction = {
        for {
          m <- MessageCRUD.dataTable.filter(_.authorId === authorId).filter(_.id === messageId)
          c <- dataTable if c.messageId === m.id
        } yield c
      }.result
      database.run(getCommentsByAuthorsMessageIdAction)
    }

    def deleteUsersComment(userId: Int, commentId: Int): Future[Int] = {
      val deleteUsersCommentAction = dataTable.filter(_.authorId === userId).filter(_.id === commentId).delete
      database.run(deleteUsersCommentAction)
    }

    def updateUsersCommentToMessage(comment: Comment): Future[Int] = {
      val updateUsersCommentToMessageAction = dataTable.filter(_.authorId === comment.authorId).
        filter(_.messageId === comment.messageId).filter(_.id === comment.id).update(comment)
      database.run(updateUsersCommentToMessageAction)
    }
  }
}
