package dev.challenge.storage.service

import dev.challenge.storage.storage.metadata.FileMetadata

import scala.concurrent.Future

trait MetadataService {

  def saveVersion(metadata: FileMetadata): Future[Unit]

  def saveMeta(metadata: FileMetadata): Future[Unit]

  def find(fileId: String): Future[Option[FileMetadata]]

  def find(fileId: String, versionId: String): Future[Option[FileMetadata]]

}
