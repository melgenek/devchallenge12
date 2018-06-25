package dev.challenge.finale.module

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.ExecutionContextExecutor

trait ActorModule extends ConfigModule {

  implicit val actorSystem: ActorSystem = ActorSystem("storage", config)

  implicit val actorMaterializer: Materializer = ActorMaterializer()(actorSystem)

  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

}
