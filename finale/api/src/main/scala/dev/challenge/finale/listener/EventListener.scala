package dev.challenge.finale.listener

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{RestartSource, Sink}
import dev.challenge.finale.config.ListenerConfig
import io.circe.Decoder
import io.circe.parser.decode
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait EventListener[T] {

  def listen(handler: T => Unit): Unit

}

class EventListenerImpl[T: Decoder](config: ListenerConfig)
                                   (implicit executionContext: ExecutionContext,
                                    materializer: Materializer,
                                    actorSystem: ActorSystem) extends EventListener[T] {

  private val consumerSettings =
    ConsumerSettings(actorSystem, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(config.kafkaConfig.bootstrapServers)
      .withGroupId(config.listenerGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  override def listen(handler: T => Unit): Unit =
    RestartSource.withBackoff(
      minBackoff = 1.seconds,
      maxBackoff = 10.seconds,
      randomFactor = 0.2
    ) { () =>
    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(config.kafkaConfig.topic))
      .map { msg =>
        val event: T = decode[T](msg.record.value).right.get
        handler(event)
        msg.committableOffset
      }
      .mapAsync(1)(_.commitScaladsl())
    }.runWith(Sink.ignore)


}

