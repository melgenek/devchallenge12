package dev.challenge.storage.config

import dev.challenge.storage.client.{InternalFileClient, SyncMetadataClient}
import dev.challenge.storage.service.MetadataServiceImpl
import dev.challenge.storage.service.download.{DownloadService, DownloadWithFallbackService, LocalDownloadService, RemoteDownloadService}
import dev.challenge.storage.service.notification.QueueNotificationService
import dev.challenge.storage.service.upload.{UploadService, UploadServiceImpl}

trait ServiceModule extends StorageModule with DiscoveryModule with QueueModule {

  val internalClient = new InternalFileClient()

  val syncMetadataClient = new SyncMetadataClient(config.getString("sync.uri"))

  val metadataService = new MetadataServiceImpl(metadataStorage, versionStorage)

  val uploadService: UploadService = new UploadServiceImpl(metadataService, fileStorage)

  val downloadService: DownloadService = {
    val localDownloadService: DownloadService = new LocalDownloadService(metadataService, internalClient, fileStorage)
    val remoteDownloadService: DownloadService = new RemoteDownloadService(syncMetadataClient, internalClient, serviceDiscoveryClient)

    new DownloadWithFallbackService(localDownloadService, remoteDownloadService)
  }

  val notificationService = new QueueNotificationService(syncQueueSender, currentServiceId)

}
