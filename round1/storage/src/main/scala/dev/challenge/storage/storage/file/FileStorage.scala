package dev.challenge.storage.storage.file

import java.io.InputStream

import scala.concurrent.Future


trait FileStorage {

  def get(objectId: String): Future[InputStream]

  def put(objectId: String, data: Array[Byte]): Future[Unit]

}
