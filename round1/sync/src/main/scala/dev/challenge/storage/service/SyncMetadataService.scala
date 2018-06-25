package dev.challenge.storage.service

import dev.challenge.storage.dto.FileMetadataWithServices
import dev.challenge.storage.storage.metadata.FileMetadata
import dev.challenge.storage.storage.sync.SyncDataStorage

import scala.concurrent.{ExecutionContext, Future}

class SyncMetadataService(metadataService: MetadataService,
                          syncDataStorage: SyncDataStorage)
                         (implicit executionContext: ExecutionContext) {

  def fileMeta(fileId: String): Future[Option[FileMetadataWithServices]] = {
    val metaFuture: Future[Option[FileMetadata]] = metadataService.find(fileId)
    metaWithServices(metaFuture)
  }

  def fileWithVersionMeta(fileId: String, version: String): Future[Option[FileMetadataWithServices]] = {
    val metaFuture: Future[Option[FileMetadata]] = metadataService.find(fileId, version)
    metaWithServices(metaFuture)
  }

  private def metaWithServices(metaFuture: Future[Option[FileMetadata]]): Future[Option[FileMetadataWithServices]] = {
    for {
      metaOpt <- metaFuture
      syncDataOpt <- metaOpt.map { meta =>
        syncDataStorage.find(meta.fileId, meta.version)
      }.getOrElse(Future.successful(None))
    } yield (metaOpt, syncDataOpt) match {
      case (Some(meta), Some(syncData)) => Some(FileMetadataWithServices(
        fileId = meta.fileId,
        version = meta.version,
        fileName = meta.fileName,
        contentType = meta.contentType,
        serviceIds = syncData.serviceIds
      ))
      case _ => None
    }
  }

}
