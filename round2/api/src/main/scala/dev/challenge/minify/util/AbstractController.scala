package dev.challenge.minify.util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.{Directives, Route}

trait AbstractController extends Directives with SprayJsonSupport {

  def route: Route

}
