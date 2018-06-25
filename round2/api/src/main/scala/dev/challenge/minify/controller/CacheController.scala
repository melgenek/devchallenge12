package dev.challenge.minify.controller

import akka.http.scaladsl.server.Route
import dev.challenge.minify.dto.external.EvictUrlRequest
import dev.challenge.minify.protocol.ExternalProtocol
import dev.challenge.minify.service.CssCacheService
import dev.challenge.minify.util.AbstractController

class CacheController(cacheService: CssCacheService) extends AbstractController with ExternalProtocol {

  override def route: Route =
    (delete & path("cache")) {
      entity(as[EvictUrlRequest]) { req =>
        complete(cacheService.delete(req.url))
      } ~ {
        complete(cacheService.deleteAll())
      }
    }

}
