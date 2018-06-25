package dev.challenge.storage.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import dev.challenge.storage.dto.{FileMetadataWithServices, FileMetadataWithServicesProtocol}
import dev.challenge.storage.service.SyncMetadataService
import dev.challenge.storage.util.AbstractController

import scala.concurrent.Future

class SyncMetadataController(syncMetadataService: SyncMetadataService)
  extends AbstractController with FileMetadataWithServicesProtocol {

  override def route: Route =
    (get & pathPrefix("meta")) {
      pathPrefix(Segment) { fileId =>
        pathEndOrSingleSlash {
          fileMeta(fileId)
        } ~ path(Segment) { version =>
          fileWithVersionMeta(fileId, version)
        }
      }
    }

  def fileMeta(fileId: String): Route = {
    val metaFuture: Future[Option[FileMetadataWithServices]] = syncMetadataService.fileMeta(fileId)
    completeMeta(metaFuture)
  }

  def fileWithVersionMeta(fileId: String, version: String): Route = {
    val metaFuture: Future[Option[FileMetadataWithServices]] = syncMetadataService.fileWithVersionMeta(fileId, version)
    completeMeta(metaFuture)
  }

  private def completeMeta(metaFuture: Future[Option[FileMetadataWithServices]]): Route = {
    onSuccess(metaFuture) {
      case Some(meta) => complete(meta)
      case None => complete(StatusCodes.NotFound)
    }
  }

}
