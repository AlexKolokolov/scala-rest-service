package org.kolokolov.service

import org.kolokolov.model.Customer
import org.kolokolov.repo.{CustomerCRUDModule, DatabaseProfile, H2Database}
import org.kolokolov.rest.DBCreator
import slick.jdbc.{H2Profile, JdbcProfile}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by Kolokolov on 10.05.2017.
  */
class CustomerService(override val profile: JdbcProfile) extends CustomerCRUDModule with DatabaseProfile {

  def getAllCustomers: Future[Seq[Customer]] = CustomerCRUD.getAll
  def getCustomerById(id: Int): Future[Option[Customer]] = CustomerCRUD.getById(id)
  def addNewCustomer(customer: Customer): Future[Int] = CustomerCRUD.save(customer)
  def deleteCustomer(id: Int): Future[Int] = CustomerCRUD.delete(id)
  def updateCustomer(customer: Customer): Future[Int] = CustomerCRUD.update(customer)
}