package dev.challenge.minify.amqp

import akka.stream.Materializer
import akka.stream.alpakka.amqp.scaladsl.AmqpRpcFlow
import akka.stream.alpakka.amqp.{AmqpCachedConnectionProvider, AmqpSinkSettings, AmqpUriConnectionProvider, IncomingMessage, OutgoingMessage, QueueDeclaration}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.util.ByteString
import com.rabbitmq.client.AMQP.BasicProperties
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

class AmqpRpcClient[Req: RootJsonWriter, Resp: RootJsonReader](queueName: String, amqpConfig: AmqpConfig)
                                                              (implicit executionContext: ExecutionContext,
                                                               materializer: Materializer) extends RpcClient[Req, Resp] {

  private val queueDeclaration = QueueDeclaration(queueName)
  private val writer = implicitly[RootJsonWriter[Req]]
  private val reader = implicitly[RootJsonReader[Resp]]

  private val connectionProvider = AmqpCachedConnectionProvider(
    AmqpUriConnectionProvider(s"amqp://${amqpConfig.user}:${amqpConfig.password}@${amqpConfig.host}:${amqpConfig.port}")
  )

  private val amqpRpcFlow: Flow[OutgoingMessage, IncomingMessage, Future[String]] = AmqpRpcFlow.atMostOnceFlow(
    AmqpSinkSettings(connectionProvider).withRoutingKey(queueName).withDeclarations(queueDeclaration), 1
  )

  override def send(message: Req): Future[Resp] = {
    Source.single(message)
      .map(m => writer.write(m).toString)
      .map { s =>
        val body = ByteString.fromString(s)
        val props = new BasicProperties().builder().expiration(amqpConfig.ttl.toMillis.toString).build()
        OutgoingMessage(body, immediate = false, mandatory = true, Some(props))
      }
      .viaMat(amqpRpcFlow)(Keep.right)
      .map(resp => reader.read(resp.bytes.utf8String.parseJson))
      .completionTimeout(amqpConfig.ttl)
      .toMat(Sink.head)(Keep.right)
      .run
  }

}
