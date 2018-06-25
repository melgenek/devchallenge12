package dev.challenge.storage.config

import com.aerospike.client.Host
import dev.challenge.storage.aerospike.{Aerospike, AerospikeConfig}

import scala.collection.JavaConverters._

trait AerospikeModule extends ConfigModule {

  val aerospikeConfig = AerospikeConfig(host = new Host(
    config.getString("aerospike.host"),
    config.getInt("aerospike.port"))
  )

  val aerospikeClientWithEventLoops = Aerospike(aerospikeConfig)

  val aerospikeNamespace: String = config.getString("aerospike.namespace")

}
