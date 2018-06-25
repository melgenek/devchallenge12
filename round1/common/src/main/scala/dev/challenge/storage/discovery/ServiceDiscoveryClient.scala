package dev.challenge.storage.discovery

trait ServiceDiscoveryClient {

  def closestStorages(): Seq[ServiceInfo]

}
