package dev.challenge.storage.controller

import akka.http.scaladsl.server.Route
import dev.challenge.storage.util.AbstractController

class IndexController extends AbstractController {

  override def route: Route =
    get {
      pathEndOrSingleSlash {
        getFromResource("pages/index.html")
      }
    }

}
