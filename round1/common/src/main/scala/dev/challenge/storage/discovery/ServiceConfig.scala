package dev.challenge.storage.discovery

case class ServiceConfig(host: String,
                         port: Int,
                         serviceName: String,
                         serviceId: String,
                         tags: Seq[String] = Seq.empty)
