package dev.challenge.minify.config

import dev.challenge.minify.amqp.{AmqpConfig, AmqpRpcClient, RpcClient}
import dev.challenge.minify.dto.internal.{InternalMinifyRequest, InternalMinifyResponse}
import dev.challenge.minify.protocol.InternalProtocol
import net.ceedubs.ficus.Ficus._

import scala.concurrent.duration.FiniteDuration

trait QueueModule extends ActorModule {

  val amqpConfig = AmqpConfig(
    host = config.as[String]("amqp.host"),
    port = config.as[Int]("amqp.port"),
    user = config.as[String]("amqp.user"),
    password = config.as[String]("amqp.password"),
    ttl = config.as[FiniteDuration]("amqp.ttl"),
  )

  val rpcClient: RpcClient[InternalMinifyRequest, InternalMinifyResponse] = {
    import InternalProtocol._
    new AmqpRpcClient[InternalMinifyRequest, InternalMinifyResponse]("minify", amqpConfig)
  }

}
