package dev.challenge.storage.queue.amqp

import akka.actor.ActorSystem
import akka.stream.Supervision.{Restart, Resume}
import akka.stream.alpakka.amqp.scaladsl.AmqpSource
import akka.stream.alpakka.amqp.{AmqpUriConnectionProvider, NamedQueueSourceSettings, QueueDeclaration}
import akka.stream.scaladsl.{RestartSource, Sink}
import akka.stream.{ActorAttributes, Materializer}
import com.typesafe.scalalogging.StrictLogging
import dev.challenge.storage.queue.QueueListener
import spray.json.{RootJsonReader, _}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class AmqpQueueListener[T: RootJsonReader](amqpConfig: AmqpConfig, queueName: String)
                                          (implicit executionContext: ExecutionContext,
                                           actorSyste: ActorSystem,
                                           materializer: Materializer) extends QueueListener[T] with StrictLogging {

  private val connectionProvider = AmqpUriConnectionProvider(s"amqp://${amqpConfig.user}:${amqpConfig.password}@${amqpConfig.host}:${amqpConfig.port}")
  private val queueDeclaration = QueueDeclaration(queueName)

  private val reader = implicitly[RootJsonReader[T]]

  private val amqpSource = RestartSource.withBackoff(
    minBackoff = 2.seconds,
    maxBackoff = 10.seconds,
    randomFactor = 0.2
  ) { () =>
    logger.info("Creating amqp source")
    AmqpSource.committableSource(
      NamedQueueSourceSettings(connectionProvider, queueName).withDeclarations(queueDeclaration),
      bufferSize = 10
    )
  }

  def listen(handler: T => Future[Unit]): Future[Unit] =
    amqpSource
      .throttle(1, 200.millis)
      .mapAsync(1) { cm =>
        val payload = cm.message.bytes.utf8String
        logger.info(s"Received message $payload")
        (for {
          obj <- Future.successful(payload).map(p => reader.read(p.parseJson))
          _ <- handler(obj)
        } yield cm).recoverWith { case e: Throwable =>
          logger.error(s"Could not handle message $payload", e)
          cm.nack().flatMap(_ => Future.failed(new MessageProcessingException(cm, e)))
        }
      }
      .mapAsync(1) { cm =>
        logger.info(s"Ack message '${new String(cm.message.bytes.toArray)}'")
        cm.ack().map(_ => cm)
      }
      .withAttributes(ActorAttributes.withSupervisionStrategy {
        case _: MessageProcessingException =>
          Resume
        case e: Throwable =>
          logger.error("Internal error", e)
          Restart
      })
      .runWith(Sink.ignore)
      .map(_ => ())

}
