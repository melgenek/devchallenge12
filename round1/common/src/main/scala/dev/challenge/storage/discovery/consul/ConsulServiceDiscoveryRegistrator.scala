package dev.challenge.storage.discovery.consul

import java.util.Optional
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}

import com.orbitz.consul.model.agent.{ImmutableRegistration, Registration}
import com.orbitz.consul.{AgentClient, Consul}
import com.typesafe.scalalogging.StrictLogging
import dev.challenge.storage.discovery.{ServiceConfig, ServiceDiscoveryRegistrator}

class ConsulServiceDiscoveryRegistrator(consul: Consul) extends ServiceDiscoveryRegistrator with StrictLogging {

  private val agentClient: AgentClient = consul.agentClient()
  private val executor = new ScheduledThreadPoolExecutor(1)

  override def pingPeriodically(serviceConfig: ServiceConfig): Unit = {
    executor.scheduleAtFixedRate(() => {
      try {
        val registration = ImmutableRegistration.builder
          .address(serviceConfig.host)
          .port(serviceConfig.port)
          .check(Optional.ofNullable(Registration.RegCheck.ttl(ConsulServiceDiscoveryRegistrator.PingPeriod)))
          .name(serviceConfig.serviceName)
          .id(serviceConfig.serviceId)
          .addTags(serviceConfig.tags: _*)
          .build
        agentClient.register(registration)
        agentClient.pass(serviceConfig.serviceId)
      } catch {
        case e: Throwable =>
          logger.error("Could not ping Consul", e)
      }
    }, 0, ConsulServiceDiscoveryRegistrator.PingPeriod, TimeUnit.SECONDS)
  }

  def shutdown(): Unit = {
    executor.shutdown()
  }

}

object ConsulServiceDiscoveryRegistrator {

  private final val PingPeriod = 5

}