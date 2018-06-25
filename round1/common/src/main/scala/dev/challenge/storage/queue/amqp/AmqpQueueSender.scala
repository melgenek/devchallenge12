package dev.challenge.storage.queue.amqp

import akka.Done
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.amqp.scaladsl.AmqpSink
import akka.stream.alpakka.amqp.{AmqpSinkSettings, AmqpUriConnectionProvider, QueueDeclaration}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import dev.challenge.storage.queue.QueueSender
import spray.json.RootJsonWriter

import scala.concurrent.{ExecutionContext, Future}

class AmqpQueueSender[T: RootJsonWriter](amqpConfig: AmqpConfig, queueName: String)
                                        (implicit executionContext: ExecutionContext,
                                         actorSyste: ActorSystem,
                                         materializer: Materializer) extends QueueSender[T] {

  private val connectionProvider = AmqpUriConnectionProvider(s"amqp://${amqpConfig.user}:${amqpConfig.password}@${amqpConfig.host}:${amqpConfig.port}")
  private val queueDeclaration = QueueDeclaration(queueName)

  private val writer = implicitly[RootJsonWriter[T]]

  private val amqpSink: Sink[ByteString, Future[Done]] = AmqpSink.simple(
    AmqpSinkSettings(connectionProvider)
      .withRoutingKey(queueName)
      .withDeclarations(queueDeclaration)
  )

  override def send(message: T): Future[Unit] =
    Source.single(message)
      .map(m => writer.write(m).toString)
      .map(s => ByteString.fromString(s))
      .runWith(amqpSink)
      .map(_ => ())

}
