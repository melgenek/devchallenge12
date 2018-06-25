package dev.challenge.storage.service

import akka.http.scaladsl.model.Uri
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import dev.challenge.storage.client.InternalFileClient
import dev.challenge.storage.discovery.{ServiceDiscoveryClient, ServiceInfo}
import dev.challenge.storage.queue.metadata.FileMetadataMessage
import dev.challenge.storage.storage.metadata.{FileMetadata, UploadStatus}
import dev.challenge.storage.storage.sync.{SyncData, SyncDataStorage}

import scala.concurrent.{ExecutionContext, Future}

class FileSyncService(internalClient: InternalFileClient,
                      syncDataStorage: SyncDataStorage,
                      discoveryClient: ServiceDiscoveryClient,
                      metadataService: MetadataService,
                      config: Config)
                     (implicit executionContext: ExecutionContext) extends StrictLogging {

  def sync(message: FileMetadataMessage): Future[Unit] = {
    val fileMetadata = FileMetadata(message.fileId, message.version, message.fileName, message.contentType, -1, -1, UploadStatus.InProgress)
    (for {
      _ <- metadataService.saveMeta(fileMetadata)
      _ <- metadataService.saveVersion(fileMetadata)
      aliveStorages = discoveryClient.closestStorages().toSet
      syncDataOpt <- syncDataStorage.find(message.fileId, message.version)
      previousReplicas = syncDataOpt.map(_.serviceIds).getOrElse(List.empty)
      fromReplica: ServiceInfo = aliveStorages.filter(s => s.serviceId == message.originalServiceId).head
      oldAliveReplicasExceptOriginal = aliveStorages
        .filter(s => previousReplicas.contains(s.serviceId))
        .filter(_.serviceId != message.originalServiceId)
        .toList
      newReplicas = aliveStorages
        .filter(s => !previousReplicas.contains(s.serviceId))
        .filter(_.serviceId != message.originalServiceId)
        .toList
      toReplicas: List[ServiceInfo] = (oldAliveReplicasExceptOriginal ++ newReplicas).take(replicationFactor)
      _ <- sync(message, fromReplica, toReplicas)
      _ <- syncDataStorage.save(SyncData(message.fileId, message.version, message.originalServiceId :: toReplicas.map(_.serviceId)))
      _ <- metadataService.saveMeta(fileMetadata.copy(uploadStatus = UploadStatus.Finished))
    } yield ()).recoverWith { case e: Throwable =>
      metadataService.saveMeta(fileMetadata.copy(uploadStatus = UploadStatus.Failed)).flatMap { _ =>
        Future.failed(e)
      }
    }
  }

  private def sync(message: FileMetadataMessage, fromReplica: ServiceInfo, toReplicas: List[ServiceInfo]): Future[Unit] = toReplicas match {
    case Nil =>
      logger.info(s"Sync complete '$message'")
      Future.successful(())
    case toReplica :: tail =>
      logger.info(s"Syncing '$message' from '$fromReplica' to '$toReplica'")
      val fileStream = internalClient.downloadFile(Uri(s"http://${fromReplica.host}:${fromReplica.port}/"), message.fileId, message.version)
      val replicaSyncFuture: Future[Unit] = for {
        _ <- internalClient.uploadFileWithoutNotification(
          Uri(s"http://${toReplica.host}:${toReplica.port}/"),
          message.fileId,
          message.version,
          message.fileName,
          message.contentType,
          fileStream
        )
        _ = logger.info(s"Successfully synced '$message' from '$fromReplica' to '$toReplica'")
      } yield ()

      replicaSyncFuture.flatMap(_ => sync(message, fromReplica, tail))
  }

  private val replicationFactor: Int = config.getInt("replication-factor")

}
