package dev.challenge.storage.storage.sync

import dev.challenge.storage.aerospike.{AerospikeClientWithEventLoops, AerospikeKVStore, KVStore}

import scala.concurrent.{ExecutionContext, Future}

class AerospikeSyncDataStorage(aerospikeClientWithEventLoops: AerospikeClientWithEventLoops, namespace: String)
                              (implicit executionContext: ExecutionContext) extends SyncDataStorage with AerospikeFormats {
  
  val datas: KVStore[(String, String), SyncData] =
    new AerospikeKVStore[(String, String), SyncData](aerospikeClientWithEventLoops, namespace, "datas")

  override def save(syncData: SyncData): Future[Unit] =
    datas.put((syncData.fileId, syncData.version), syncData)

  override def find(fileId: String, version: String): Future[Option[SyncData]] =
    datas.find(fileId, version)

}
