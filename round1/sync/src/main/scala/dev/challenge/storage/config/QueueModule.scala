package dev.challenge.storage.config

import dev.challenge.storage.queue.QueueListener
import dev.challenge.storage.queue.amqp.{AmqpConfig, AmqpQueueListener}
import dev.challenge.storage.queue.metadata.{FileMetadataMessage, FileMetadataMessageProtocol}

trait QueueModule extends ActorModule with FileMetadataMessageProtocol {

  val amqpConfig = AmqpConfig(
    host = config.getString("amqp.host"),
    port = config.getInt("amqp.port"),
    user = config.getString("amqp.user"),
    password = config.getString("amqp.password")
  )

  val syncQueue: String = config.getString("sync.queue.name")

  val syncQueueListener: QueueListener[FileMetadataMessage] = new AmqpQueueListener[FileMetadataMessage](amqpConfig, syncQueue)

}
