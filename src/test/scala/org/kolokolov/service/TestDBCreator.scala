package org.kolokolov.service

import org.kolokolov.model.{Comment, Message, User}
import org.kolokolov.repo.{CommentCRUDModule, DatabaseProfile, MessageCRUDModule, UserCRUDModule}
import slick.jdbc.H2Profile

import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class TestDBCreator extends UserCRUDModule with MessageCRUDModule with CommentCRUDModule {

  self: DatabaseProfile =>

  import profile.api._

  def setupDB: Future[Unit] = {
    val setup = DBIO.seq(
      UserCRUD.dataTable.schema.create,
      MessageCRUD.dataTable.schema.create,
      CommentCRUD.dataTable.schema.create,
      UserCRUD.dataTable ++= Seq(User("Bob Marley"), User("Tom Waits"), User("Guy Pearce")),
      MessageCRUD.dataTable ++= Seq(Message("Rock sucks!", 1), Message("Good morning to everyone!", 1), Message("My new album has been released!", 2), Message("Happy New Year!", 3)),
      CommentCRUD.dataTable ++= Seq(Comment("Shut up! Your reggae sucks!",1,2), Comment("Great! I love it!",2,3), Comment("Thank you, buddy!",3,2), Comment("Thank you, man!",3,1))
    ).transactionally
    database.run(setup)
  }

  def cleanDB: Future[Unit] = {
    val dropTables = DBIO.seq(
      CommentCRUD.dataTable.schema.drop,
      MessageCRUD.dataTable.schema.drop,
      UserCRUD.dataTable.schema.drop
    ).transactionally
    database.run(dropTables)
  }
}
