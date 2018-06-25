package dev.challenge.storage.service.upload

import java.util.UUID

import akka.NotUsed
import akka.http.scaladsl.model.ContentType
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import dev.challenge.storage.service.MetadataService
import dev.challenge.storage.storage.file.FileStorage
import dev.challenge.storage.storage.metadata.{FileMetadata, UploadStatus}
import dev.challenge.storage.util.ChunkStage

import scala.concurrent.{ExecutionContext, Future}


class UploadServiceImpl(metadataService: MetadataService,
                        storage: FileStorage)
                       (implicit executionContext: ExecutionContext,
                        materializer: Materializer) extends UploadService with StrictLogging {

  override def uploadFile(fileName: String, contentType: ContentType, fileStream: Source[ByteString, Any]): Future[FileMetadata] = {
    val fileId: String = generateFileId()
    val version: String = generateVersion()
    logger.info(s"Uploading new file '$fileId : $version' with name '$fileName'")
    val initialMetadata = FileMetadata(fileId, version, fileName, contentType, 0, 0, UploadStatus.InProgress)
    uploadAndSaveMeta(initialMetadata, fileStream)
  }

  override def uploadFile(fileId: String, fileName: String, contentType: ContentType, fileStream: Source[ByteString, Any]): Future[FileMetadata] =
    metadataService.find(fileId).map {
      _.map(_.copy(version = generateVersion()))
        .getOrElse(FileMetadata(fileId, generateVersion(), fileName, contentType, 0, 0, UploadStatus.InProgress))
    }.flatMap { metadata =>
      uploadAndSaveMeta(metadata, fileStream)
    }

  override def uploadFile(fileId: String, version: String, fileName: String, contentType: ContentType, fileStream: Source[ByteString, Any]): Future[FileMetadata] =
    metadataService.find(fileId, version).map {
      _.getOrElse(FileMetadata(fileId, version, fileName, contentType, 0, 0, UploadStatus.InProgress))
    }.flatMap { metadata =>
      uploadAndSaveMeta(metadata, fileStream)
    }

  private def uploadAndSaveMeta(initialMetadata: FileMetadata, fileStream: Source[ByteString, Any]): Future[FileMetadata] = {
    val resultMetadataFuture: Future[FileMetadata] = for {
      _ <- metadataService.saveMeta(initialMetadata)
      _ <- metadataService.saveVersion(initialMetadata)
      (chunkCount, fileLength) <- uploadFileStream(fileStream, initialMetadata.generateChunkIds)
      finishedMetadata = initialMetadata.copy(chunkCount = chunkCount, fileLength = fileLength, uploadStatus = UploadStatus.Finished)
      _ <- metadataService.saveMeta(finishedMetadata)
    } yield initialMetadata

    resultMetadataFuture.recoverWith { case e: Throwable =>
      metadataService.saveMeta(initialMetadata.copy(uploadStatus = UploadStatus.Failed)).flatMap { _ =>
        Future.failed(e)
      }
    }
  }

  private def uploadFileStream(fileStream: Source[ByteString, Any], chunkIds: Source[String, NotUsed]): Future[(Long, Long)] =
    fileStream
      .via(new ChunkStage(UploadServiceImpl.ChunkSize))
      .zip(chunkIds)
      .mapAsyncUnordered(4) {
        case (rawChunk, chunkId) =>
          logger.debug(s"Uploading $chunkId with size: ${rawChunk.length}")
          storage.put(chunkId, rawChunk.toArray).map(_ => rawChunk.length)
      }
      .runFold((0L, 0L)) { (acc, downloadedSize) =>
        (acc._1 + 1, acc._2 + downloadedSize)
      }

  private def generateFileId(): String = UUID.randomUUID().toString

  private def generateVersion(): String = UUID.randomUUID().toString

}

object UploadServiceImpl {
  final val ChunkSize = 5 * 1024 * 1024
}
