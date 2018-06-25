package dev.challenge.storage.service.download

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import dev.challenge.storage.client.InternalFileClient
import dev.challenge.storage.service.MetadataService
import dev.challenge.storage.storage.file.FileStorage
import dev.challenge.storage.storage.metadata.FileMetadata

import scala.concurrent.{ExecutionContext, Future}


class LocalDownloadService(metadataService: MetadataService,
                           internalClient: InternalFileClient,
                           storage: FileStorage)
                          (implicit executionContext: ExecutionContext, materializer: Materializer) extends DownloadService {

  override def downloadFile(fileId: String): Future[Option[(FileMetadata, Source[ByteString, Any])]] =
    for {
      metadataOpt <- metadataService.find(fileId)
    } yield metadataOpt.map(metadata => (metadata, fileStream(metadata)))

  override def downloadFile(fileId: String, version: String): Future[Option[(FileMetadata, Source[ByteString, Any])]] =
    for {
      metadataOpt <- metadataService.find(fileId, version)
    } yield metadataOpt.map(metadata => (metadata, fileStream(metadata)))

  private def fileStream(metadata: FileMetadata): Source[ByteString, Any] = {
    def chunkStream(chunkId: String): Source[ByteString, NotUsed] =
      Source
        .fromFuture(storage.get(chunkId))
        .flatMapConcat(in => StreamConverters.fromInputStream(() => in))

    Source(metadata.chunkIds.toList).flatMapConcat(chunkStream)
  }

}
