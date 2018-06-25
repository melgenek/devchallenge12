package dev.challenge.storage.discovery

trait ServiceDiscoveryRegistrator {

  def pingPeriodically(serviceConfig: ServiceConfig)

}
