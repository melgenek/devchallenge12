package dev.challenge.storage.service.upload

import akka.http.scaladsl.model.ContentType
import akka.stream.scaladsl.Source
import akka.util.ByteString
import dev.challenge.storage.storage.metadata.FileMetadata

import scala.concurrent.Future


trait UploadService {

  def uploadFile(fileName: String,
                 contentType: ContentType,
                 fileStream: Source[ByteString, Any]): Future[FileMetadata]

  def uploadFile(fileId: String,
                 fileName: String,
                 contentType: ContentType,
                 fileStream: Source[ByteString, Any]): Future[FileMetadata]

  def uploadFile(fileId: String,
                 version: String,
                 fileName: String,
                 contentType: ContentType,
                 fileStream: Source[ByteString, Any]): Future[FileMetadata]

}
