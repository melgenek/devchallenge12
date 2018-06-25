package dev.challenge.storage.config

import akka.dispatch.MessageDispatcher
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import dev.challenge.storage.storage.file.{FileStorage, S3FileStorage}
import dev.challenge.storage.storage.metadata.AerospikeMetadataStorage
import dev.challenge.storage.storage.version.{AerospikeVersionStorage, VersionStorage}

trait StorageModule extends ActorModule with AerospikeModule {

  val fileStorage: FileStorage = {
    val s3Client: AmazonS3 = AmazonS3ClientBuilder
      .standard()
      .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(config.getString("s3.endpoint"), config.getString("s3.region")))
      .withPathStyleAccessEnabled(true)
      .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(config.getString("s3.accessKey"), config.getString("s3.secretKey"))))
      .build()
    val s3ExecutionContext: MessageDispatcher = actorSystem.dispatchers.lookup("s3-dispatcher")
    new S3FileStorage(s3Client, config.getString("s3.bucket"))(s3ExecutionContext)
  }

  val metadataStorage = new AerospikeMetadataStorage(aerospikeClientWithEventLoops, aerospikeNamespace)

  val versionStorage: VersionStorage = new AerospikeVersionStorage(aerospikeClientWithEventLoops, aerospikeNamespace)

}
