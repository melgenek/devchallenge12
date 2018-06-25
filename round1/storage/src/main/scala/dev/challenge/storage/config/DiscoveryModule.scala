package dev.challenge.storage.config

import com.google.common.net.HostAndPort
import com.orbitz.consul.Consul
import dev.challenge.storage.discovery.consul.{ConsulConfig, ConsulServiceDiscoveryClient, ConsulServiceDiscoveryRegistrator}
import dev.challenge.storage.discovery.{ServiceConfig, ServiceDiscoveryClient, ServiceDiscoveryRegistrator}

trait DiscoveryModule extends ConfigModule {

  val currentServiceId: String = config.getString("discovery.service.id")

  val serviceConfig = ServiceConfig(
    config.getString("discovery.service.host"),
    config.getInt("discovery.service.port"),
    config.getString("discovery.service.name"),
    currentServiceId,
    Seq(config.getString("discovery.service.region"))
  )

  val consulConfig = ConsulConfig(
    config.getString("discovery.consul.host"),
    config.getInt("discovery.consul.port")
  )
  val consul: Consul = Consul.builder
    .withHostAndPort(HostAndPort.fromParts(consulConfig.host, consulConfig.port))
    .withPing(false)
    .build()

  val serviceDiscoveryRegistrator: ServiceDiscoveryRegistrator = {
    val registrator = new ConsulServiceDiscoveryRegistrator(consul)
    registrator.pingPeriodically(serviceConfig)
    sys.addShutdownHook {
      registrator.shutdown()
    }
    registrator
  }

  val serviceDiscoveryClient: ServiceDiscoveryClient = new ConsulServiceDiscoveryClient(consul)

}
