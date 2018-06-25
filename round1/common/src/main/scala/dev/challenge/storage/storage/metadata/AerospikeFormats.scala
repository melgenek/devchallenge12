package dev.challenge.storage.storage.metadata

import akka.http.scaladsl.model.ContentType
import com.aerospike.client.{Bin, Record}
import dev.challenge.storage.aerospike.{KeyFormat, ValueFormat}

trait AerospikeFormats {

  implicit val compositeKeyFormat: KeyFormat[(String, String)] = (key: (String, String)) => s"${key._1}_${key._2}"

  implicit val fileMetadataFormat: ValueFormat[FileMetadata] = new ValueFormat[FileMetadata] {
    override def read(record: Record): FileMetadata = FileMetadata(
      fileId = record.getString("fileId"),
      version = record.getString("version"),
      fileName = record.getString("fileName"),
      contentType = ContentType.parse(record.getString("contentType")).right.get,
      chunkCount = record.getLong("chunkCount"),
      fileLength = record.getLong("fileLength"),
      uploadStatus = UploadStatus.withName(record.getString("uploadStatus"))
    )

    override def write(record: FileMetadata): Seq[Bin] = Seq(
      record.fileId.asBin("fileId"),
      record.version.asBin("version"),
      record.fileName.asBin("fileName"),
      record.contentType.toString.asBin("contentType"),
      record.chunkCount.asBin("chunkCount"),
      record.fileLength.asBin("fileLength"),
      record.uploadStatus.toString.asBin("uploadStatus")
    )
  }


}
