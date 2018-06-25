package dev.challenge.minify.cache.aerospike

import com.aerospike.client.listener.{DeleteListener, RecordListener, WriteListener}
import com.aerospike.client.policy.WritePolicy
import com.aerospike.client.{AerospikeException, Key, Record}
import dev.challenge.minify.cache.AsyncCache

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}


class AerospikeCache[K: KeyFormat, V: ValueFormat](clientWithEventLoops: AerospikeClientWithEventLoops,
                                                   namespace: String,
                                                   set: String) extends AsyncCache[K, V] {

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
            val res: Option[V] = Option(record).map(valueFormat.read)
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

  override def put(key: K, value: V, ttl: FiniteDuration): Future[Unit] = {
    val p: Promise[Unit] = Promise[Unit]

    val writePolicy = new WritePolicy(client.getWritePolicyDefault)
    writePolicy.expiration = ttl.toSeconds.toInt

    client.put(
      eventLoops.next(),
      new WriteListener {
        override def onFailure(exception: AerospikeException): Unit =
          p.failure(exception)

        override def onSuccess(key: Key): Unit =
          p.success(())
      },
      writePolicy,
      new Key(namespace, set, keyFormat.write(key)),
      valueFormat.write(value): _*
    )
    p.future
  }

  override def delete(key: K): Future[Unit] = {
    val p: Promise[Unit] = Promise[Unit]
    client.delete(
      eventLoops.next(),
      new DeleteListener {
        override def onSuccess(key: Key, existed: Boolean): Unit =
          p.success(())

        override def onFailure(exception: AerospikeException): Unit =
          p.failure(exception)
      },
      null,
      new Key(namespace, set, keyFormat.write(key)),
    )
    p.future
  }

  override def deleteAll(): Future[Unit] =
    Future.successful(client.truncate(null, namespace, set, null))

}
