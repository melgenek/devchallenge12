package dev.challenge.storage.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.{Get, Put}
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.Multipart.FormData.BodyPart
import akka.http.scaladsl.model.{ContentType, HttpEntity, Uri}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import akka.{Done, NotUsed}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class InternalFileClient(implicit executionContext: ExecutionContext,
                         actorSyste: ActorSystem,
                         materializer: Materializer) extends StrictLogging {

  private val http = Http().superPool[NotUsed]()

  def downloadFile(hostUri: Uri, fileId: String): Source[ByteString, NotUsed] = {
    download(resolveDownloadUri(hostUri, fileId))
  }

  def downloadFile(hostUri: Uri, fileId: String, version: String): Source[ByteString, NotUsed] = {
    download(resolveDownloadUri(hostUri, fileId, version))
  }

  private def download(uri: Uri) =
    Source.single(Get(uri) -> NotUsed)
      .via(http)
      .flatMapConcat {
        case (Success(response), _) => response.entity.dataBytes
        case (Failure(e), _) =>
          logger.error(s"Could not reach '$uri'")
          Source.failed(e)
      }

  def uploadFile(hostUri: Uri,
                 fileId: String,
                 version: String,
                 fileName: String,
                 contentType: ContentType,
                 fileStream: Source[ByteString, Any]): Future[Done] = {
    val uri = resolveUploadUri(hostUri, fileId, version)
    upload(uri, fileName, contentType, fileStream)
  }

  def uploadFileWithoutNotification(hostUri: Uri,
                                    fileId: String,
                                    version: String,
                                    fileName: String,
                                    contentType: ContentType,
                                    fileStream: Source[ByteString, Any]): Future[Done] = {
    val uri = resolveUploadWithoutNotificationUri(hostUri, fileId, version)
    upload(uri, fileName, contentType, fileStream)
  }

  private def upload(uri: Uri, fileName: String, contentType: ContentType, fileStream: Source[ByteString, Any]): Future[Done] = {
    val bodyData = HttpEntity.IndefiniteLength(contentType, fileStream)
    val bodyPart = BodyPart("fileData", bodyData, Map("filename" -> fileName))
    val body = FormData(bodyPart)

    Source.single(Put(uri, body) -> NotUsed)
      .via(http)
      .mapAsync(1) {
        case (Success(response), _) =>
          Future.successful(())
        case (Failure(e), _) =>
          logger.error(s"Could not reach '$uri'")
          Future.failed(e)
      }.runWith(Sink.ignore)
  }

  private def resolveDownloadUri(hostUri: Uri, fileId: String): Uri =
    Uri(s"/download/$fileId").resolvedAgainst(hostUri)

  private def resolveDownloadUri(hostUri: Uri, fileId: String, version: String): Uri =
    Uri(s"/download/$fileId/$version").resolvedAgainst(hostUri)

  private def resolveUploadUri(hostUri: Uri, fileId: String, version: String): Uri =
    Uri(s"/upload/$fileId/$version").resolvedAgainst(hostUri)

  private def resolveUploadWithoutNotificationUri(hostUri: Uri, fileId: String, version: String): Uri =
    Uri(s"/internal/upload/$fileId/$version").resolvedAgainst(hostUri)


}
