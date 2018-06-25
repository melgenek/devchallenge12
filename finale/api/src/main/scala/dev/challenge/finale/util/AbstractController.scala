package dev.challenge.finale.util

import akka.http.scaladsl.server.{Directives, Route}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

trait AbstractController extends Directives with FailFastCirceSupport {

  def route: Route

}
