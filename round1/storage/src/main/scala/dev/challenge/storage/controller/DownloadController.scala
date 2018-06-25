package dev.challenge.storage.controller

import akka.http.scaladsl.model.headers.{ContentDispositionTypes, RawHeader, `Content-Disposition`, `Content-Length`}
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import dev.challenge.storage.service.download.DownloadService
import dev.challenge.storage.storage.metadata.FileMetadata
import dev.challenge.storage.util.AbstractController

import scala.concurrent.Future
import scala.util.{Failure, Success}

class DownloadController(downloadService: DownloadService) extends AbstractController with StrictLogging {

  override def route: Route =
    (get & pathPrefix("download")) {
      pathPrefix(Segment) { fileId =>
        pathEndOrSingleSlash {
          downloadFile(fileId)
        } ~ path(Segment) { version =>
          downloadFileWithVersion(fileId, version)
        }
      }
    }

  protected def downloadFile(fileId: String): Route = {
    val fileStreamFuture: Future[Option[(FileMetadata, Source[ByteString, Any])]] = downloadService.downloadFile(fileId)
    completeDownload(fileId, fileStreamFuture)
  }

  protected def downloadFileWithVersion(fileId: String, version: String): Route = {
    val fileStreamFuture: Future[Option[(FileMetadata, Source[ByteString, Any])]] = downloadService.downloadFile(fileId, version)
    completeDownload(fileId, fileStreamFuture)
  }

  private def completeDownload(fileId: String, fileStreamFuture: Future[Option[(FileMetadata, Source[ByteString, Any])]]): Route = {
    onComplete(fileStreamFuture) {
      case Success(Some((metadata, stream))) =>
        respondWithHeaders(
          `Content-Length`(metadata.fileLength),
          `Content-Disposition`(ContentDispositionTypes.attachment, Map("filename" -> metadata.fileName)),
          RawHeader("version", metadata.version)) {
          complete(HttpEntity(metadata.contentType, stream))
        }
      case Success(None) =>
        logger.debug(s"File not found '$fileId'")
        complete(StatusCodes.NotFound)
      case Failure(e) =>
        logger.error(s"Error occured downloading file '$fileId'", e)
        complete(StatusCodes.InternalServerError)
    }
  }

}
