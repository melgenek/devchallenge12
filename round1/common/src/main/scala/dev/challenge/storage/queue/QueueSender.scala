package dev.challenge.storage.queue

import scala.concurrent.Future


trait QueueSender[T] {

  def send(message: T): Future[Unit]

}
