
package dev.challenge.storage.storage.metadata

import akka.NotUsed
import akka.http.scaladsl.model.ContentType
import akka.stream.scaladsl.Source

case class FileMetadata(fileId: String,
                        version: String,
                        fileName: String,
                        contentType: ContentType,
                        chunkCount: Long,
                        fileLength: Long,
                        uploadStatus: UploadStatus.Value) {

  def chunkIds: Seq[String] = (1L to chunkCount).map(index => s"${fileId}_${version}_$index")

  def generateChunkIds: Source[String, NotUsed] = {
    Source(Stream.from(1).map(index => s"${fileId}_${version}_$index"))
  }

}

object UploadStatus extends Enumeration {

  val InProgress: Value = Value
  val Failed: Value = Value
  val Finished: Value = Value

}
