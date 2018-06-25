package dev.challenge.storage.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import dev.challenge.storage.dto.{UploadProtocol, UploadResponse}
import dev.challenge.storage.service.upload.UploadService
import dev.challenge.storage.storage.metadata.FileMetadata
import dev.challenge.storage.util.AbstractController

import scala.concurrent.Future
import scala.util.{Failure, Success}

class InternalUploadController (uploadService: UploadService) extends AbstractController with StrictLogging with UploadProtocol {

  override def route: Route =
    (pathPrefix("internal" / "upload") & fileUpload("fileData")) { case (fileInfo, bodyStream) =>
      put {
        path(Segment / Segment) { (fileId, version) =>
          uploadFileWithIdAndVersion(fileId, version, fileInfo, bodyStream)
        }
      }
    }

  protected def uploadFileWithIdAndVersion(fileId: String, version: String, fileInfo: FileInfo, bodyStream: Source[ByteString, Any]): Route = {
    val fileUploadFuture: Future[FileMetadata] = uploadService.uploadFile(fileId, version, fileInfo.fileName, fileInfo.contentType, bodyStream)
    completeUpload(fileUploadFuture)
  }

  private def completeUpload(fileUploadFuture: Future[FileMetadata]): Route =
    onComplete(fileUploadFuture) {
      case Success(metadata) =>
        logger.info(s"Successfully completed internal file upload '${metadata.fileId}' with version '${metadata.version}'")
        complete(UploadResponse(metadata.fileId, metadata.version))
      case Failure(e) =>
        logger.error("Was not able to upload file", e)
        complete(StatusCodes.InternalServerError)
    }

}
