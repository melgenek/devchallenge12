package dev.challenge.storage.storage.version

import com.aerospike.client.{Bin, Record}
import dev.challenge.storage.aerospike.{KeyFormat, ValueFormat}

import scala.collection.JavaConverters._

trait AerospikeFormats {

  implicit val keyFormat: KeyFormat[String] = (key: String) => key

  implicit val fileMetadataFormat: ValueFormat[VersionData] = new ValueFormat[VersionData] {
    override def read(record: Record): VersionData = VersionData(
      fileId = record.getString("fileId"),
      versions = record.getList("versions").asScala.map(_.toString).toList
    )

    override def write(record: VersionData): Seq[Bin] = Seq(
      record.fileId.asBin("fileId"),
      record.versions.asBin("versions")
    )
  }

}
