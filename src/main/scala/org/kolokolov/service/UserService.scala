package org.kolokolov.service

import org.kolokolov.model.{Customer, Message}
import org.kolokolov.repo.{CustomerCRUDModule, DatabaseProfile}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Kolokolov on 10.05.2017.
  */
class UserService(override val profile: JdbcProfile)(implicit val ec: ExecutionContext) extends CustomerCRUDModule with DatabaseProfile {

  def getAllUsers: Future[Seq[Customer]] = CustomerCRUD.getAll
  def getUserById(id: Int): Future[Option[Customer]] = CustomerCRUD.getById(id)
  def saveUser(user: Customer): Future[Int] = CustomerCRUD.save(user)
  def deleteUser(id: Int): Future[Int] = CustomerCRUD.delete(id)
  def updateUser(user: Customer): Future[Int] = CustomerCRUD.update(user)
}
