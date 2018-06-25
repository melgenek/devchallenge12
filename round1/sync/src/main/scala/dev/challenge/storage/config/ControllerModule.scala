package dev.challenge.storage.config

import akka.http.scaladsl.server.{Directives, Route}
import dev.challenge.storage.controller.SyncMetadataController
import dev.challenge.storage.util.AbstractController

trait ControllerModule extends ServiceModule with Directives {

  val metaController: SyncMetadataController = new SyncMetadataController(syncMetadataService)

  val controllers: Set[AbstractController] = Set(
    metaController
  )

  val routes: Route = controllers.foldLeft[Route](reject)(_ ~ _.route)

}
