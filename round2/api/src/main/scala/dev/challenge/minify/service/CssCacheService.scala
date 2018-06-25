package dev.challenge.minify.service

import dev.challenge.minify.cache.AsyncCache
import dev.challenge.minify.dto.external.{Redirect, UrlCss, UrlResponse}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

trait CssCacheService {

  def find(url: String): Future[Option[UrlResponse]]

  def save(urlResponse: UrlResponse): Future[Unit]

  def delete(url: String): Future[Unit]

  def deleteAll(): Future[Unit]

}

class CssCacheServiceImpl(cssCache: AsyncCache[String, String],
                          redirectCache: AsyncCache[String, String],
                          ttl: FiniteDuration)
                         (implicit executionContext: ExecutionContext) extends CssCacheService {

  override def find(url: String): Future[Option[UrlResponse]] =
    for {
      cssOpt <- cssCache.find(url)
      urlOpt <- redirectCache.find(url)
    } yield cssOpt
      .map(css => Some(UrlCss(url, css)))
      .getOrElse(urlOpt.map(Redirect))

  override def save(urlResponse: UrlResponse): Future[Unit] = urlResponse match {
    case UrlCss(url, css) => cssCache.put(url, css, ttl)
    case Redirect(url) => redirectCache.put(url, url, ttl)
  }

  override def delete(url: String): Future[Unit] =
    for {
      _ <- cssCache.delete(url)
      _ <- redirectCache.delete(url)
    } yield ()

  override def deleteAll(): Future[Unit] =
    for {
      _ <- cssCache.deleteAll()
      _ <- redirectCache.deleteAll()
    } yield ()

}
