package dev.challenge.minify.config

import com.aerospike.client.Host
import dev.challenge.minify.cache.aerospike.SimpleFormats._
import dev.challenge.minify.cache.aerospike.{Aerospike, AerospikeCache, AerospikeConfig}

trait CacheModule extends ActorModule {

  val aerospikeConfig = AerospikeConfig(host = new Host(
    config.getString("aerospike.host"),
    config.getInt("aerospike.port"))
  )

  val aerospikeClientWithEventLoops = Aerospike(aerospikeConfig)

  val aerospikeNamespace: String = config.getString("aerospike.namespace")

  val cssCache = new AerospikeCache[String, String](aerospikeClientWithEventLoops, aerospikeNamespace, "css")

  val redirectCache = new AerospikeCache[String, String](aerospikeClientWithEventLoops, aerospikeNamespace, "redirects")

}
