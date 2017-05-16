package org.kolokolov.service

import org.kolokolov.model.Car
import org.kolokolov.repo.{BodyTypeCRUDModule, CarCRUDModule, DatabaseProfile}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class CarService(override val profile: JdbcProfile) extends CarCRUDModule with BodyTypeCRUDModule with DatabaseProfile {

  def getAllCars: Future[Seq[Car]] = CarCRUD.getAll
  def getCarById(id: Int): Future[Option[Car]] = CarCRUD.getById(id)
  def saveCar(car: Car): Future[Int] = CarCRUD.save(car)
  def deleteCar(id: Int): Future[Int] = CarCRUD.delete(id)
  def updateCar(car: Car): Future[Int] = CarCRUD.update(car)
  def getCarsByBodyTypeId(bodyTypeId: Int): Future[Seq[Car]] = CarCRUD.getCarsByBodyType(bodyTypeId)
}
