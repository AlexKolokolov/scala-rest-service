package org.kolokolov.repo

import org.kolokolov.model.Message

import scala.concurrent.Future

/**
  * Created by Kolokolov on 16.05.2017.
  */
trait MessageCRUDModule extends AbstractCRUDModule {

  self: CustomerCRUDModule with DatabaseProfile =>

  import profile.api._

  class MessageTable(tag: Tag) extends Table[Message](tag, "message_table") with IdentifiableTable[Message] {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def text = column[String]("text")
    def authorId = column[Int]("author_id")
    def author = foreignKey("author", authorId, CustomerCRUD.dataTable)(_.id,
      onDelete = ForeignKeyAction.Cascade, onUpdate = ForeignKeyAction.Restrict)
    def * = (text, authorId, id) <> (Message.tupled, Message.unapply)
  }

  object MessageCRUD extends AbstractCRUD[Message, MessageTable] {
    lazy val dataTable: TableQuery[MessageTable] = TableQuery[MessageTable]

    def getMessagesByAuthorId(authorId: Int): Future[Seq[Message]] = {
      val getCarsByBodyTypeAction = dataTable.filter(_.authorId === authorId).result
      database.run(getCarsByBodyTypeAction)
    }

    def getMessageByAuthorAndMessageIds(authorId: Int, messageId: Int): Future[Option[Message]] = {
      val getMessageByAuthorAndMessageIdsAction = dataTable.filter(_.authorId === authorId).filter(_.id === messageId).result.headOption
      database.run(getMessageByAuthorAndMessageIdsAction)
    }

    def updateUsersMessage(message: Message): Future[Int] = {
      val updateUsersMessageAction = dataTable.filter(_.authorId === message.authorId).filter(_.id === message.id).update(message)
      database.run(updateUsersMessageAction)
    }

    def deleteUsersMessage(userId: Int, messageId: Int): Future[Int] = {
      val deleteUsersMessageAction = dataTable.filter(_.authorId === userId).filter(_.id === messageId).delete
      database.run(deleteUsersMessageAction)
    }
  }
}
