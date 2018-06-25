package dev.challenge.storage.service.notification

import com.typesafe.scalalogging.StrictLogging
import dev.challenge.storage.queue.QueueSender
import dev.challenge.storage.queue.metadata.FileMetadataMessage
import dev.challenge.storage.storage.metadata.FileMetadata

import scala.concurrent.{ExecutionContext, Future}

class QueueNotificationService(queueSender: QueueSender[FileMetadataMessage],
                               currentServiceId: String)
                              (implicit executionContext: ExecutionContext) extends NotificationService with StrictLogging {

  override def notifyUpload(fileMetadata: FileMetadata): Future[Unit] =
    queueSender.send(FileMetadataMessage(
      originalServiceId = currentServiceId,
      fileId = fileMetadata.fileId,
      version = fileMetadata.version,
      fileName = fileMetadata.fileName,
      contentType = fileMetadata.contentType
    )).recoverWith { case e: Throwable =>
      logger.error("Could not send sync message", e)
      Future.successful(())
    }

}
