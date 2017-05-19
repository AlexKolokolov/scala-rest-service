package org.kolokolov.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.kolokolov.model.{Comment, Message, User}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  * Created by Kolokolov on 11.05.2017.
  */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User)
  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat3(Message)
  implicit val commentFormat: RootJsonFormat[Comment] = jsonFormat4(Comment)
}
