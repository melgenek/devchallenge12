package dev.challenge.storage.config

import dev.challenge.storage.client.InternalFileClient
import dev.challenge.storage.service.{FileSyncService, MetadataServiceImpl, SyncMetadataService}

trait ServiceModule extends StorageModule with DiscoveryModule {

  val internalClient = new InternalFileClient()

  val metadataService = new MetadataServiceImpl(metadataStorage, versionStorage)

  val syncService = new FileSyncService(internalClient, syncDataStorage, serviceDiscoveryClient, metadataService, config)

  val syncMetadataService = new SyncMetadataService(metadataService, syncDataStorage)

}
