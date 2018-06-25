package dev.challenge.storage.storage.file

import java.io.{ByteArrayInputStream, InputStream}

import com.amazonaws.event.ProgressEventType._
import com.amazonaws.event.{ProgressEvent, ProgressListener}
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.transfer.{TransferManagerBuilder, Upload}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

class S3FileStorage(s3Client: AmazonS3, bucketName: String)
                   (implicit executionContext: ExecutionContext) extends FileStorage with StrictLogging {

  private val transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build()

  override def get(objectId: String): Future[InputStream] = {
    Future(s3Client.getObject(bucketName, objectId).getObjectContent)(executionContext)
  }

  override def put(objectId: String, data: Array[Byte]): Future[Unit] = {
    val metadata = new ObjectMetadata()
    metadata.setContentLength(data.length.toLong)
    val upload: Upload = transferManager.upload(bucketName, objectId, new ByteArrayInputStream(data), metadata)

    val promise: Promise[Unit] = Promise[Unit]()
    upload.addProgressListener(new ProgressListener {
      override def progressChanged(progressEvent: ProgressEvent): Unit = {
        if (progressEvent.getEventType == TRANSFER_FAILED_EVENT ||
          progressEvent.getEventType == TRANSFER_CANCELED_EVENT ||
          progressEvent.getEventType == TRANSFER_COMPLETED_EVENT) {
          promise.complete(Try(upload.waitForUploadResult()).map(_ => ()))
        }
      }
    })
    promise.future.recoverWith {
      case e: Exception =>
        logger.error(s"Could not upload $objectId")
        Future.failed(e)
    }

  }


}
