package dev.challenge.minify.amqp

import scala.concurrent.Future

trait RpcClient[Req, Resp] {

  def send(message: Req): Future[Resp]

}
