package dev.challenge.storage.storage.version

import scala.concurrent.Future

trait VersionStorage {

  def save(versionData: VersionData): Future[Unit]

  def find(fileId: String): Future[Option[VersionData]]

}
