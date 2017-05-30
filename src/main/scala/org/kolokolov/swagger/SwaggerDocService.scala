package org.kolokolov.swagger

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.swagger.akka.{HasActorSystem, SwaggerHttpService}
import org.kolokolov.rest.SwaggerShopRestController

import reflect.runtime.universe._

/**
  * Created by andersen on 25.05.2017.
  */
class SwaggerDocService(system: ActorSystem) extends SwaggerHttpService with HasActorSystem {
  override implicit val actorSystem: ActorSystem = system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override val apiTypes = Seq(typeOf[SwaggerShopRestController])
  override val host = "localhost:8080" //the url of your api, not swagger's json endpoint
  override val basePath = "/webapi"    //the basePath for the API you are exposing
  override val apiDocsPath = "/api-docs" //where you want the swagger-json endpoint exposed
}
