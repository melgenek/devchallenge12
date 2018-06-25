package dev.challenge.finale.service

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import dev.challenge.finale.config.KafkaConfig
import dev.challenge.finale.dto.DataEvent
import io.circe.Encoder
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.{ExecutionContext, Future}

trait MessageService {

  def send[T >: DataEvent : Encoder](key: String, event: T): Future[Unit]

}

class MessageServiceImpl(kafkaConfig: KafkaConfig)
                        (implicit executionContext: ExecutionContext,
                         materializer: Materializer,
                         actorSystem: ActorSystem) extends MessageService {

  private val producerSettings: ProducerSettings[String, String] =
    ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
      .withBootstrapServers(kafkaConfig.bootstrapServers)

  private val kafkaProducer: KafkaProducer[String, String] =
    producerSettings.createKafkaProducer()

  override def send[T >: DataEvent : Encoder](key: String, event: T): Future[Unit] = {
    val encoder: Encoder[T] = implicitly[Encoder[T]]
    Source.single(event)
      .map(encoder.apply)
      .map(_.toString)
      .map(value => new ProducerRecord[String, String](kafkaConfig.topic, key, value))
      .runWith(Producer.plainSink(producerSettings, kafkaProducer))
      .map(_ => ())
  }

}

