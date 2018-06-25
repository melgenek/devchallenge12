package dev.challenge.storage.service.download

import akka.NotUsed
import akka.http.scaladsl.model.Uri
import akka.stream.scaladsl.Source
import akka.util.ByteString
import dev.challenge.storage.client.{InternalFileClient, SyncMetadataClient}
import dev.challenge.storage.discovery.{ServiceDiscoveryClient, ServiceInfo}
import dev.challenge.storage.dto.FileMetadataWithServices
import dev.challenge.storage.storage.metadata.{FileMetadata, UploadStatus}

import scala.concurrent.{ExecutionContext, Future}

class RemoteDownloadService(syncMetadataClient: SyncMetadataClient,
                            internalClient: InternalFileClient,
                            discoveryClient: ServiceDiscoveryClient)
                           (implicit executionContext: ExecutionContext) extends DownloadService {

  override def downloadFile(fileId: String): Future[Option[(FileMetadata, Source[ByteString, Any])]] =
    for {
      metadataOpt <- syncMetadataClient.meta(fileId)
    } yield metadataOpt.flatMap(fileStreamWithMetadata)

  override def downloadFile(fileId: String, version: String): Future[Option[(FileMetadata, Source[ByteString, Any])]] =
    for {
      metadataOpt <- syncMetadataClient.meta(fileId, version)
    } yield metadataOpt.flatMap(fileStreamWithMetadata)

  private def fileStreamWithMetadata(metadata: FileMetadataWithServices): Option[(FileMetadata, Source[ByteString, NotUsed])] = {
    val closestStorages: Seq[ServiceInfo] = discoveryClient.closestStorages()
    val replicaOpt: Option[ServiceInfo] = closestStorages.find(s => metadata.serviceIds.contains(s.serviceId))
    replicaOpt.flatMap { replica =>
      val stream: Source[ByteString, NotUsed] = internalClient.downloadFile(Uri(s"http://${replica.host}:${replica.port}/"), metadata.fileId, metadata.version)
      val fileMetadata = FileMetadata(
        fileId = metadata.fileId,
        version = metadata.version,
        contentType = metadata.contentType,
        fileName = metadata.fileName,
        chunkCount = -1L,
        fileLength = -1L,
        uploadStatus = UploadStatus.Finished
      )
      Some(fileMetadata, stream)
    }
  }


}
