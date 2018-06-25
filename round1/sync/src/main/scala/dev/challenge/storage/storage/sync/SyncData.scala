
package dev.challenge.storage.storage.sync

case class SyncData(fileId: String,
                    version: String,
                    serviceIds: List[String] = List.empty)
