package org.kolokolov.service

import org.kolokolov.model.{Comment, Message, Customer}
import org.kolokolov.repo.{CommentCRUDModule, DatabaseProfile, MessageCRUDModule, CustomerCRUDModule}
import slick.jdbc.H2Profile

import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class TestDBCreator extends CustomerCRUDModule with MessageCRUDModule with CommentCRUDModule {

  self: DatabaseProfile =>

  import profile.api._

  def setupDB: Future[Unit] = {
    val setup = DBIO.seq(
      CustomerCRUD.dataTable.schema.create,
      MessageCRUD.dataTable.schema.create,
      CommentCRUD.dataTable.schema.create,
      CustomerCRUD.dataTable ++= Seq(Customer("Bob Marley"), Customer("Tom Waits"), Customer("Guy Pearce")),
      MessageCRUD.dataTable ++= Seq(Message("Rock sucks!", 1), Message("Good morning to everyone!", 1), Message("My new album has been released!", 2), Message("Happy New Year!", 3)),
      CommentCRUD.dataTable ++= Seq(Comment("Shut up! Your reggae sucks!",1,2), Comment("Great! I love it!",3,3), Comment("Thank you, buddy!",4,2), Comment("Thank you, man!",4,1))
    ).transactionally
    database.run(setup)
  }

  def cleanDB: Future[Unit] = {
    val dropTables = DBIO.seq(
      CommentCRUD.dataTable.schema.drop,
      MessageCRUD.dataTable.schema.drop,
      CustomerCRUD.dataTable.schema.drop
    ).transactionally
    database.run(dropTables)
  }
}
