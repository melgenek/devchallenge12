package dev.challenge.minify.config

import akka.http.scaladsl.server.{Directives, Route}
import dev.challenge.minify.controller.{CacheController, MinifyController}
import dev.challenge.minify.util.AbstractController

trait ControllerModule extends ServiceModule with Directives {

  val minifyController = new MinifyController(minifyService)

  val cacheController = new CacheController(cssCacheService)

  val controllers: Set[AbstractController] = Set(
    minifyController,
    cacheController
  )

  val routes: Route = controllers.foldLeft[Route](reject)(_ ~ _.route)

}
