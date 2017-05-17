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
  def deleteMessage(id: Int): Future[Int] = CommentCRUD.delete(id)
  def updateMessage(comment: Comment): Future[Int] = CommentCRUD.update(comment)
  def getCommentsByAuthorId(authorId: Int): Future[Seq[Comment]] = CommentCRUD.getCommentsByAuthorId(authorId)
  def getCommentsByMessageId(messageId: Int): Future[Seq[Comment]] = CommentCRUD.getCommentsByMessageId(messageId)
}
