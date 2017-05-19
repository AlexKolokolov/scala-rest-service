package org.kolokolov.service

import org.kolokolov.model.{Message, User}
import org.kolokolov.repo.{DatabaseProfile, UserCRUDModule}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class UserService(override val profile: JdbcProfile) extends UserCRUDModule with DatabaseProfile {

  def getAllUsers: Future[Seq[User]] = UserCRUD.getAll
  def getUserById(id: Int): Future[Option[User]] = UserCRUD.getById(id)
  def saveUser(user: User): Future[Int] = UserCRUD.save(user)
  def deleteUser(id: Int): Future[Int] = UserCRUD.delete(id)
  def updateUser(user: User): Future[Int] = UserCRUD.update(user)
}
