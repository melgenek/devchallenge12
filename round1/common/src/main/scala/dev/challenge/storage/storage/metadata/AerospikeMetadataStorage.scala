package dev.challenge.storage.storage.metadata

import dev.challenge.storage.aerospike.{AerospikeClientWithEventLoops, AerospikeKVStore, KVStore}

import scala.concurrent.{ExecutionContext, Future}

class AerospikeMetadataStorage(aerospikeClientWithEventLoops: AerospikeClientWithEventLoops, namespace: String)
                              (implicit executionContext: ExecutionContext) extends MetadataStorage with AerospikeFormats {

  val metas: KVStore[(String, String), FileMetadata] =
    new AerospikeKVStore[(String, String), FileMetadata](aerospikeClientWithEventLoops, namespace, "metadata")

  override def save(metadata: FileMetadata): Future[Unit] =
    metas.put((metadata.fileId, metadata.version), metadata)

  override def find(fileId: String, versionId: String): Future[Option[FileMetadata]] =
    metas.find(fileId, versionId)

}
