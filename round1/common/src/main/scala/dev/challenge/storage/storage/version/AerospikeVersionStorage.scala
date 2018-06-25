package dev.challenge.storage.storage.version

import dev.challenge.storage.aerospike.{AerospikeClientWithEventLoops, AerospikeKVStore, KVStore}

import scala.concurrent.{ExecutionContext, Future}

class AerospikeVersionStorage(aerospikeClientWithEventLoops: AerospikeClientWithEventLoops,
                              namespace: String)
                             (implicit executionContext: ExecutionContext) extends VersionStorage with AerospikeFormats {

  val versions: KVStore[String, VersionData] =
    new AerospikeKVStore[String, VersionData](aerospikeClientWithEventLoops, namespace, "versions")

  override def save(versionData: VersionData): Future[Unit] =
    versions.put(versionData.fileId, versionData)

  override def find(fileId: String): Future[Option[VersionData]] =
    versions.find(fileId)

}
