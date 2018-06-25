package dev.challenge.minify.config

import dev.challenge.minify.service.{CssCacheServiceImpl, MinifyServiceImpl, ProcessorServiceImpl}
import net.ceedubs.ficus.Ficus._

import scala.concurrent.duration.FiniteDuration

trait ServiceModule extends CacheModule with QueueModule {

  val cacheTtlTimeout: FiniteDuration = config.as[FiniteDuration]("minify.cache.ttl")
  val cssCacheService = new CssCacheServiceImpl(cssCache, redirectCache, cacheTtlTimeout)

  val processorService = new ProcessorServiceImpl(rpcClient)

  val responseTimeout: FiniteDuration = config.as[FiniteDuration]("minify.timeout")
  val storeToCache: Boolean = config.getOrElse[Boolean]("cache", true)
  val minifyService = new MinifyServiceImpl(processorService, cssCacheService, responseTimeout, storeToCache)

}
