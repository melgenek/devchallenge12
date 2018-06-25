package dev.challenge.storage.dto

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class UploadResponse(fileId: String, version: String)

trait UploadProtocol extends DefaultJsonProtocol {

  implicit val uploadResponseProtocol: RootJsonFormat[UploadResponse] = jsonFormat2(UploadResponse)

}