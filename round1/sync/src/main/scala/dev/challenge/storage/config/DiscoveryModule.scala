package dev.challenge.storage.config

import com.google.common.net.HostAndPort
import com.orbitz.consul.Consul
import dev.challenge.storage.discovery.ServiceDiscoveryClient
import dev.challenge.storage.discovery.consul.{ConsulConfig, ConsulServiceDiscoveryClient}

trait DiscoveryModule extends ConfigModule {

  val consulConfig = ConsulConfig(
    config.getString("discovery.consul.host"),
    config.getInt("discovery.consul.port")
  )

  val serviceDiscoveryClient: ServiceDiscoveryClient = {
    val consul: Consul = Consul.builder
      .withHostAndPort(HostAndPort.fromParts(consulConfig.host, consulConfig.port))
      .withPing(false)
      .build()
    new ConsulServiceDiscoveryClient(consul)
  }

}
