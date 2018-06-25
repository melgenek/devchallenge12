package dev.challenge.minify.service

import akka.actor.ActorSystem
import com.typesafe.scalalogging.StrictLogging
import dev.challenge.minify.dto.external.{MinifyResponse, UrlCss, UrlResponse}
import dev.challenge.minify.util.FutureExtensions.TimeoutOps

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

trait MinifyService {

  def minify(urls: Set[String]): Future[MinifyResponse]

}


class MinifyServiceImpl(processorService: ProcessorService,
                        cacheService: CssCacheService,
                        timeout: FiniteDuration,
                        storeToCache: Boolean)
                       (implicit executionContext: ExecutionContext,
                        actorSystem: ActorSystem) extends MinifyService with StrictLogging {

  override def minify(urls: Set[String]): Future[MinifyResponse] =
    for {
      responsesOpt <- Future.traverse(urls)(minify)
      responses: Set[UrlResponse] = responsesOpt.flatten
    } yield {
      val results: Set[UrlCss] = responses.collect { case r: UrlCss => r }
      MinifyResponse(results)
    }

  private def minify(url: String): Future[Option[UrlResponse]] = {
    for {
      cachedOpt <- if (storeToCache) cacheService.find(url) else Future.successful(None)
      urlResponse <- cachedOpt.map(cached => Future.successful(Some(cached)))
        .getOrElse(processAndCache(url).withTimeout(None, timeout))
    } yield urlResponse
  }

  private def processAndCache(url: String): Future[Option[UrlResponse]] = {
    processorService.process(url).flatMap {
      _.map { processed =>
        if (storeToCache) cacheService.save(processed).map(_ => Some(processed))
        else Future.successful(Some(processed))
      }.getOrElse(Future.successful(None))
    }
  }

}
