package dev.challenge.finale.service

import com.redis.RedisClient
import dev.challenge.finale.config.RedisConfig
import dev.challenge.finale.model.ValueModel
import dev.challenge.finale.protocol.ModelProtocol
import io.circe.parser.decode
import io.circe.syntax._

trait StorageService {

  def save(name: String, model: ValueModel): Unit

  def saveIfNotExists(name: String, model: ValueModel): Unit

  def find(name: String): Option[ValueModel]

  def delete(name: String): Unit

  def history(name: String): Option[Seq[ValueModel]]

  def getAll: Seq[(String, Seq[ValueModel])]

}

class StorageServiceImpl(redisConfig: RedisConfig) extends StorageService with ModelProtocol {

  private val client = new RedisClient(redisConfig.host, redisConfig.port)

  override def save(name: String, model: ValueModel): Unit =
    client.lpush(name, model.asJson.toString)

  override def saveIfNotExists(name: String, model: ValueModel): Unit = {
    val alreadyStored: Boolean = history(name).exists(_.equals(model))
    if (!alreadyStored) save(name, model)
  }

  override def find(name: String): Option[ValueModel] =
    client.lindex(name, 0)
      .flatMap(v => decode[ValueModel](v).toOption)

  override def delete(name: String): Unit =
    client.del(name)

  override def history(name: String): Option[Seq[ValueModel]] =
    client.lrange(name, 0, -1)
      .map(_.flatten)
      .map(_.map(v => decode[ValueModel](v).toOption))
      .map(_.flatten)

  override def getAll: Seq[(String, Seq[ValueModel])] = {
    client.scan(0, count = Integer.MAX_VALUE)
      .flatMap(_._2)
      .map {
        _.flatten.map(name => (name, history(name)))
          .collect {
            case (name, Some(values)) => (name, values)
          }.toSeq
      }
  }.getOrElse(Seq.empty)

}
