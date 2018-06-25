package dev.challenge.storage.service

import dev.challenge.storage.storage.metadata.{FileMetadata, MetadataStorage, UploadStatus}
import dev.challenge.storage.storage.version.{VersionData, VersionStorage}

import scala.concurrent.{ExecutionContext, Future}

class MetadataServiceImpl(metadataStorage: MetadataStorage, versionStorage: VersionStorage)
                         (implicit executionContext: ExecutionContext) extends MetadataService {

  def saveVersion(metadata: FileMetadata): Future[Unit] =
    for {
      versionDataOpt <- versionStorage.find(metadata.fileId)
      versionData: VersionData = versionDataOpt.getOrElse(VersionData(metadata.fileId))
      _ <- if (versionData.versions.contains(metadata.version)) Future.successful(())
      else versionStorage.save(versionData.copy(versions = metadata.version :: versionData.versions))
    } yield ()

  def saveMeta(metadata: FileMetadata): Future[Unit] =
    metadataStorage.save(metadata)

  def find(fileId: String): Future[Option[FileMetadata]] =
    for {
      versionDataOpt <- versionStorage.find(fileId)
      uploadedVersionOpt <- versionDataOpt.map { fileData =>
        Future.traverse(fileData.versions) { version =>
          metadataStorage.find(fileId, version)
        }.map { metadatas =>
          metadatas.flatten.find(fm => fm.uploadStatus == UploadStatus.Finished)
        }
      }.getOrElse(Future.successful(None))
    } yield uploadedVersionOpt


  def find(fileId: String, versionId: String): Future[Option[FileMetadata]] =
    metadataStorage.find(fileId, versionId)

}
