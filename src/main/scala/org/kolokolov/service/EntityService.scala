package org.kolokolov.service

import org.kolokolov.model.Entity
import org.kolokolov.repo.{DatabaseProfile, EntityCRUDModule}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by Kolokolov on 10.05.2017.
  */
class EntityService(override val profile: JdbcProfile) extends EntityCRUDModule with DatabaseProfile{

  def getAllEntities: Future[Seq[Entity]] = EntityCRUD.getAll
  def getEntityById(id: Int): Future[Option[Entity]] = EntityCRUD.getById(id)
  def saveEntity(entity: Entity): Future[Int] = EntityCRUD.save(entity)
  def deleteEntity(id: Int): Future[Int] = EntityCRUD.delete(id)
  def updateEntity(entity: Entity): Future[Int] = EntityCRUD.update(entity)
}
