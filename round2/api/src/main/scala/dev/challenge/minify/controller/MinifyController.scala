package dev.challenge.minify.controller

import akka.http.scaladsl.server.Route
import dev.challenge.minify.dto.external.MinifyRequest
import dev.challenge.minify.protocol.ExternalProtocol
import dev.challenge.minify.service.MinifyService
import dev.challenge.minify.util.AbstractController

class MinifyController(minifyService: MinifyService) extends AbstractController with ExternalProtocol {

  override def route: Route = (post & path("minify")) {
    entity(as[MinifyRequest]) { req =>
      complete(minifyService.minify(req.urls))
    }
  }

}
