package dev.challenge.storage.service.download

import akka.stream.scaladsl.Source
import akka.util.ByteString
import dev.challenge.storage.storage.metadata.FileMetadata

import scala.concurrent.Future


trait DownloadService {

  def downloadFile(fileId: String): Future[Option[(FileMetadata, Source[ByteString, Any])]]

  def downloadFile(fileId: String, version: String): Future[Option[(FileMetadata, Source[ByteString, Any])]]

}