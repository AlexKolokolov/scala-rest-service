package org.kolokolov.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.kolokolov.model.Entity
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  * Created by andersen on 11.05.2017.
  */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val entityFormat: RootJsonFormat[Entity] = jsonFormat2(Entity)
}
