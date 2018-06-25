package dev.challenge.minify.cache

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait AsyncCache[K, V] {

  def find(key: K): Future[Option[V]]

  def put(key: K, value: V, timeout: FiniteDuration): Future[Unit]

  def delete(key: K): Future[Unit]

  def deleteAll(): Future[Unit]

}
