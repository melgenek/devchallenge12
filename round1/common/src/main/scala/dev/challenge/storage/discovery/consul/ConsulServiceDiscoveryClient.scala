package dev.challenge.storage.discovery.consul

import com.orbitz.consul.option.ImmutableQueryOptions
import com.orbitz.consul.{Consul, HealthClient}
import com.typesafe.scalalogging.StrictLogging
import dev.challenge.storage.discovery.{ServiceDiscoveryClient, ServiceInfo}

import scala.collection.JavaConverters._

class ConsulServiceDiscoveryClient(consul: Consul) extends ServiceDiscoveryClient with StrictLogging {

  private val catalogClient = consul.catalogClient()
  private val healthClient: HealthClient = consul.healthClient()

  override def closestStorages(): Seq[ServiceInfo] = {
    val datacenters: Seq[String] = catalogClient.getDatacenters.asScala
    val result = datacenters.flatMap(dc => getHealthyServiceNodesInDc("storage", dc))
    logger.info(s"Storage nodes are following: $result")
    result
  }

  private def getHealthyServiceNodesInDc(serviceName: String, dc: String): Seq[ServiceInfo] =
    healthClient.getHealthyServiceInstances(serviceName, ImmutableQueryOptions.builder
      .datacenter(dc)
      .near("_agent")
      .build()
    ).getResponse.asScala
      .map { health =>
        val service = health.getService
        ServiceInfo(service.getId, service.getAddress, service.getPort)
      }

}
