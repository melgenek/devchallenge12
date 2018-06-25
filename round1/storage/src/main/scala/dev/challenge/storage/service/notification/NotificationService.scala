package dev.challenge.storage.service.notification

import dev.challenge.storage.storage.metadata.FileMetadata

import scala.concurrent.Future

trait NotificationService {

  def notifyUpload(fileMetadata: FileMetadata): Future[Unit]

}
