package dev.challenge.storage.storage.sync

import scala.concurrent.Future

trait SyncDataStorage {

  def save(syncData: SyncData): Future[Unit]

  def find(fileId: String, version: String): Future[Option[SyncData]]

}
