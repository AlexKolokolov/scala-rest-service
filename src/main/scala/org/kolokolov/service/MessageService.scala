package org.kolokolov.service

import org.kolokolov.model.Message
import org.kolokolov.repo.{DatabaseProfile, MessageCRUDModule, UserCRUDModule}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class MessageService(override val profile: JdbcProfile) extends MessageCRUDModule with UserCRUDModule with DatabaseProfile {

  def getAllMessages: Future[Seq[Message]] = MessageCRUD.getAll
  def getMessageById(id: Int): Future[Option[Message]] = MessageCRUD.getById(id)
  def saveMessage(message: Message): Future[Int] = MessageCRUD.save(message)
  def deleteMessage(messageId: Int): Future[Int] = MessageCRUD.delete(messageId)
  def deleteUsersMessage(userId: Int, messageId: Int): Future[Int] = MessageCRUD.deleteUsersMessage(userId, messageId)
  def updateUsersMessage(message: Message): Future[Int] = MessageCRUD.updateUsersMessage(message)
  def getMessagesByAuthorId(authorId: Int): Future[Seq[Message]] = MessageCRUD.getMessagesByAuthorId(authorId)
  def getUsersMessageById(userId: Int, messageId: Int): Future[Option[Message]] = MessageCRUD.getMessageByAuthorAndMessageIds(userId,messageId)
}
