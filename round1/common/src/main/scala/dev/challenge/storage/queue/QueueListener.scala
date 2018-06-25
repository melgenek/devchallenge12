package dev.challenge.storage.queue

import scala.concurrent.Future

trait QueueListener[T] {

  def listen(handler: T => Future[Unit]): Future[Unit]

}
