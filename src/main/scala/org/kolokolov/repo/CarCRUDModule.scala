package org.kolokolov.repo

import org.kolokolov.model.Car

import scala.concurrent.Future

/**
  * Created by Kolokolov on 16.05.2017.
  */
trait CarCRUDModule extends AbstractCRUDModule {

  self: BodyTypeCRUDModule with DatabaseProfile =>

  import profile.api._

  class CarTable(tag: Tag) extends Table[Car](tag, "car_table") with IdentifiableTable[Car] {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def make = column[String]("make")
    def model = column[String]("model")
    def bodyTypeId = column[Int]("body_type_id")
    def bodyType = foreignKey("body_type", bodyTypeId, BodyTypeCRUD.dataTable)(_.id,
      onDelete = ForeignKeyAction.Cascade, onUpdate = ForeignKeyAction.Restrict)
    def * = (make, model, bodyTypeId, id) <> (Car.tupled, Car.unapply)
  }

  object CarCRUD extends AbstractCRUD[Car, CarTable] {
    lazy val dataTable: TableQuery[CarTable] = TableQuery[CarTable]

    def getCarsByBodyType(bodyTypeId: Int): Future[Seq[Car]] = {
      val getCarsByBodyTypeAction = dataTable.filter(_.bodyTypeId === bodyTypeId).result
      database.run(getCarsByBodyTypeAction)
    }
  }
}
