package dev.challenge.minify.service

import com.typesafe.scalalogging.StrictLogging
import dev.challenge.minify.amqp.RpcClient
import dev.challenge.minify.dto.external.{Redirect, UrlCss, UrlResponse}
import dev.challenge.minify.dto.internal.{ErrorResponse, InternalMinifyRequest, InternalMinifyResponse, RedirectResponse, SuccessResponse}

import scala.concurrent.{ExecutionContext, Future}

trait ProcessorService {

  def process(url: String): Future[Option[UrlResponse]]

}

class ProcessorServiceImpl(rpcClient: RpcClient[InternalMinifyRequest, InternalMinifyResponse])
                          (implicit executionContext: ExecutionContext) extends ProcessorService with StrictLogging {

  override def process(url: String): Future[Option[UrlResponse]] =
    rpcClient.send(InternalMinifyRequest(url))
      .map(processInternalResponse)
      .recover { case e: Throwable =>
        logger.error(s"An unexpected error occurred while processing $url'", e)
        None
      }

  private def processInternalResponse(internalResponse: InternalMinifyResponse): Option[UrlResponse] =
    internalResponse match {
      case SuccessResponse(url, css) =>
        Some(UrlCss(url, css))
      case RedirectResponse(url) =>
        Some(Redirect(url))
      case ErrorResponse(url, message) =>
        logger.info(s"An error occurred while processing '$url': '$message'")
        None
    }

}
