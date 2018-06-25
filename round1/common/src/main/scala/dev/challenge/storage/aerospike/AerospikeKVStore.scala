package dev.challenge.storage.aerospike

import com.aerospike.client.listener.{RecordListener, WriteListener}
import com.aerospike.client.{AerospikeException, Key, Record}

import scala.concurrent.{Future, Promise}
import scala.util.Try

/**
  * Wrapper over java client
  */
class AerospikeKVStore[K: KeyFormat, V: ValueFormat](clientWithEventLoops: AerospikeClientWithEventLoops,
                                                     namespace: String,
                                                     set: String) extends KVStore[K, V] {

  private val AerospikeClientWithEventLoops(client, eventLoops) = clientWithEventLoops
  private val keyFormat: KeyFormat[K] = implicitly[KeyFormat[K]]
  private val valueFormat: ValueFormat[V] = implicitly[ValueFormat[V]]

  override def find(key: K): Future[Option[V]] = {
    val p: Promise[Option[V]] = Promise[Option[V]]

    client.get(
      eventLoops.next(),
      new RecordListener {
        override def onFailure(exception: AerospikeException): Unit =
          p.failure(exception)

        override def onSuccess(key: Key, record: Record): Unit = {
          try {
            val res = Option(record).map(valueFormat.read)
            p.success(res)
          } catch {
            case e: Throwable => p.failure(e)
          }
        }
      },
      null,
      new Key(namespace, set, keyFormat.write(key))
    )

    p.future
  }

  override def put(key: K, value: V): Future[Unit] = {
    val p: Promise[Unit] = Promise[Unit]

    client.put(
      eventLoops.next(),
      new WriteListener {
        override def onFailure(exception: AerospikeException): Unit =
          p.failure(exception)

        override def onSuccess(key: Key): Unit =
          p.success(())
      },
      null,
      new Key(namespace, set, keyFormat.write(key)),
      valueFormat.write(value): _*
    )

    p.future
  }

}
