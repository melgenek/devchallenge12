package dev.challenge.storage.queue.metadata

import akka.http.scaladsl.model.ContentType
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

import scala.util.Try

case class FileMetadataMessage(originalServiceId: String,
                               fileId: String,
                               version: String,
                               fileName: String,
                               contentType: ContentType)

trait FileMetadataMessageProtocol extends DefaultJsonProtocol {

  implicit val contentTypeFormat: JsonFormat[ContentType] = new JsonFormat[ContentType] {
    override def read(json: JsValue): ContentType =
      Try(json.asInstanceOf[JsString])
        .map(s => ContentType.parse(s.value).right.get)
        .getOrElse(throw DeserializationException("Wrong ContentType format"))

    override def write(contentType: ContentType): JsValue = JsString(contentType.toString)
  }

  implicit val fileMetadataMessageFormat: RootJsonFormat[FileMetadataMessage] = jsonFormat5(FileMetadataMessage)

}
