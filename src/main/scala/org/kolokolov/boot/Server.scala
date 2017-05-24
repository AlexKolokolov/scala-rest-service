package org.kolokolov.boot

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import org.kolokolov.repo.H2Database
import org.kolokolov.rest.{DBCreator, ShopRestController}
import org.kolokolov.service.{CommentService, MessageService, UserService}
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

/**
  * Created by Kolokolov on 11.05.2017.
  */
object Server {

  implicit val system = ActorSystem("rest-service-actor-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val userService = new UserService(H2Profile)
  val messageService = new MessageService(H2Profile)
  val commentService = new CommentService(H2Profile)

  val shopRestController = new ShopRestController(system) with H2Database
  val dbHelper = new DBCreator with H2Database

  def main(args: Array[String]) {

    Await.result(dbHelper.setupDB, Duration.Inf)

    val bindingFuture = Http().bindAndHandle(shopRestController.rootRoute, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
