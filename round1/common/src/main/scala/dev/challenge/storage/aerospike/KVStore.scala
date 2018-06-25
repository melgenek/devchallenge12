package dev.challenge.storage.aerospike

import scala.concurrent.Future

trait KVStore[K, V] {

  def find(key: K): Future[Option[V]]

  def put(key: K, value: V): Future[Unit]

}
