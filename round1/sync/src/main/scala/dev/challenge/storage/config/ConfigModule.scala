package dev.challenge.storage.config

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigModule {

  val config: Config = ConfigFactory.load()

}
