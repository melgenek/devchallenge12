package dev.challenge.storage.storage.metadata

import scala.concurrent.Future

trait MetadataStorage {

  def save(metadata: FileMetadata): Future[Unit]

  def find(fileId: String, versionId: String): Future[Option[FileMetadata]]

}
