package dev.challenge.finale.module


import akka.http.scaladsl.server.{Directives, Route}
import com.softwaremill.macwire._
import dev.challenge.finale.controller.StorageController
import dev.challenge.finale.util.AbstractController

class ControllerModule extends ServiceModule with Directives {

  val storageController: StorageController = wire[StorageController]

  val controllers: Set[AbstractController] = wireSet[AbstractController]

  val routes: Route = controllers.foldLeft[Route](reject)(_ ~ _.route)

}
