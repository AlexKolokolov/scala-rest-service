package org.kolokolov.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.kolokolov.model.{BodyType, Car, Entity}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  * Created by Kolokolov on 11.05.2017.
  */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val entityFormat: RootJsonFormat[Entity] = jsonFormat2(Entity)
  implicit val carFormat: RootJsonFormat[Car] = jsonFormat4(Car)
  implicit val bodyTypeFormat: RootJsonFormat[BodyType] = jsonFormat2(BodyType)
}
