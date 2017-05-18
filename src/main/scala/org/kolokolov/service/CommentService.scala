package org.kolokolov.service

import org.kolokolov.model.Comment
import org.kolokolov.repo.{CommentCRUDModule, DatabaseProfile, MessageCRUDModule, UserCRUDModule}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class CommentService(override val profile: JdbcProfile) extends CommentCRUDModule with UserCRUDModule with MessageCRUDModule with DatabaseProfile {

  def getAllComments: Future[Seq[Comment]] = CommentCRUD.getAll
  def getCommentById(id: Int): Future[Option[Comment]] = CommentCRUD.getById(id)
  def saveComment(comment: Comment): Future[Int] = CommentCRUD.save(comment)
  def deleteComment(id: Int): Future[Int] = CommentCRUD.delete(id)
  def deleteUsersComment(userId: Int, commentId: Int): Future[Int] = CommentCRUD.deleteUsersComment(userId,commentId)
  def updateUsersCommentToMessage(comment: Comment): Future[Int] = CommentCRUD.updateUsersCommentToMessage(comment)
  def getCommentsByAuthorId(authorId: Int): Future[Seq[Comment]] = CommentCRUD.getCommentsByAuthorId(authorId)
  def getCommentsByMessageId(messageId: Int): Future[Seq[Comment]] = CommentCRUD.getCommentsByMessageId(messageId)
  def getCommentsByAuthorsMessageId(authorId: Int, messageId: Int): Future[Seq[Comment]] = CommentCRUD.getCommentsByAuthorsMessageId(authorId,messageId)
}
