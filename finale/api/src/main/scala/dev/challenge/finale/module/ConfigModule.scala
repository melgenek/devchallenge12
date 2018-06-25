
package dev.challenge.finale.module

import com.typesafe.config.{Config, ConfigFactory}
import dev.challenge.finale.config.{ConsulConfig, KafkaConfig, ListenerConfig, RedisConfig}

trait ConfigModule {

  val config: Config = ConfigFactory.load()

  val kafkaConfig: KafkaConfig = KafkaConfig(
    config.getString("messaging.bootstrapServers"),
    config.getString("messaging.topic")
  )

  val listenerConfig = ListenerConfig(
    kafkaConfig,
    config.getString("messaging.group")
  )

  val redisConfig: RedisConfig = RedisConfig(
    config.getString("redis.host"),
    config.getInt("redis.port")
  )

  val consulConfig = ConsulConfig(
    config.getString("consul.host"),
    config.getInt("consul.port"),
    config.getString("consul.service"),
  )

}
