package dev.challenge.storage.queue.amqp

import akka.stream.alpakka.amqp.scaladsl.CommittableIncomingMessage


class MessageProcessingException(val msg: CommittableIncomingMessage, cause: Throwable) extends RuntimeException(cause)
