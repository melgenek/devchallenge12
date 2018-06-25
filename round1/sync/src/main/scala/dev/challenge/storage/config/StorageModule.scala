package dev.challenge.storage.config

import dev.challenge.storage.storage.metadata.AerospikeMetadataStorage
import dev.challenge.storage.storage.sync.{AerospikeSyncDataStorage, SyncDataStorage}
import dev.challenge.storage.storage.version.{AerospikeVersionStorage, VersionStorage}

trait StorageModule extends AerospikeModule with ActorModule {

  val syncDataStorage: SyncDataStorage = new AerospikeSyncDataStorage(aerospikeClientWithEventLoops, aerospikeNamespace)

  val metadataStorage = new AerospikeMetadataStorage(aerospikeClientWithEventLoops, aerospikeNamespace)

  val versionStorage: VersionStorage = new AerospikeVersionStorage(aerospikeClientWithEventLoops, aerospikeNamespace)

}
