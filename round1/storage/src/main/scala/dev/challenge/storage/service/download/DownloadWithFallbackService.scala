package dev.challenge.storage.service.download

import akka.stream.scaladsl.Source
import akka.util.ByteString
import dev.challenge.storage.storage.metadata.FileMetadata

import scala.concurrent.{ExecutionContext, Future}

class DownloadWithFallbackService(localDownloadService: DownloadService,
                                  remoteDownloadService: DownloadService)
                                 (implicit executionContext: ExecutionContext) extends DownloadService {

  override def downloadFile(fileId: String): Future[Option[(FileMetadata, Source[ByteString, Any])]] =
    for {
      localOpt <- localDownloadService.downloadFile(fileId)
      res <- localOpt
        .map(local => Future.successful(Some(local)))
        .getOrElse(remoteDownloadService.downloadFile(fileId))
    } yield res

  override def downloadFile(fileId: String, version: String): Future[Option[(FileMetadata, Source[ByteString, Any])]] =
    for {
      localOpt <- localDownloadService.downloadFile(fileId, version)
      res <- localOpt
        .map(local => Future.successful(Some(local)))
        .getOrElse(remoteDownloadService.downloadFile(fileId, version))
    } yield res

}
