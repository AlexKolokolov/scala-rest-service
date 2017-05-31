package org.kolokolov.boot

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.github.swagger.akka.SwaggerSite
import org.kolokolov.repo.{H2Database, PostgresDatabase}
import org.kolokolov.rest.{DBCreator, RestController}
import org.kolokolov.swagger.SwaggerDocService
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

/**
  * Created by Kolokolov on 11.05.2017.
  */
object Server extends App with SwaggerSite {

  implicit val system = ActorSystem("rest-service-actor-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val shopRestController = new RestController(system) with PostgresDatabase
  val swagger = new SwaggerDocService(system)
//  val dbHelper = new DBCreator with PostgresDatabase

  val routes = Route {
    swaggerSiteRoute ~ swagger.routes ~
    pathPrefix("webapi") {
      shopRestController.routes
    }
  }

//  Await.result(dbHelper.setupDB, Duration.Inf)

  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
