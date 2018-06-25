package dev.challenge.minify.util

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext

object TestContext {
  private val actorSystem: ActorSystem = ActorSystem("test")

  private val executionContext: ExecutionContext = actorSystem.dispatcher
}

trait TestContext {
  implicit val actorSystem: ActorSystem = TestContext.actorSystem
  implicit val executionContext: ExecutionContext = TestContext.executionContext
}
