package dev.challenge.minify.amqp

import scala.concurrent.duration._

case class AmqpConfig(host: String, port: Int, user: String, password: String, ttl: FiniteDuration = 5.seconds)

