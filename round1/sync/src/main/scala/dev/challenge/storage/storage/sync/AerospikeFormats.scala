package dev.challenge.storage.storage.sync

import com.aerospike.client.{Bin, Record}
import dev.challenge.storage.aerospike.{KeyFormat, ValueFormat}

import scala.collection.JavaConverters._

trait AerospikeFormats {

  implicit val compositeKeyFormat: KeyFormat[(String, String)] = (key: (String, String)) => s"${key._1}_${key._2}"

  implicit val fileMetadataFormat: ValueFormat[SyncData] = new ValueFormat[SyncData] {
    override def read(record: Record): SyncData = SyncData(
      fileId = record.getString("fileId"),
      version = record.getString("version"),
      serviceIds = record.getList("serviceIds").asScala.map(_.toString).toList
    )

    override def write(record: SyncData): Seq[Bin] = Seq(
      record.fileId.asBin("fileId"),
      record.version.asBin("version"),
      record.serviceIds.asBin("serviceIds")
    )
  }

}
