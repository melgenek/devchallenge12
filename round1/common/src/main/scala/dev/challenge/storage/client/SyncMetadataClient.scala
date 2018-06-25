package dev.challenge.storage.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import dev.challenge.storage.dto.{FileMetadataWithServices, FileMetadataWithServicesProtocol}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class SyncMetadataClient(syncHostUri: String)
                        (implicit executionContext: ExecutionContext,
                         actorSyste: ActorSystem,
                         materializer: Materializer)
  extends FileMetadataWithServicesProtocol with SprayJsonSupport with StrictLogging {

  private val http = Http()

  def meta(fileId: String): Future[Option[FileMetadataWithServices]] = {
    getMeta(resolveMetaUri(fileId))
  }

  def meta(fileId: String, version: String): Future[Option[FileMetadataWithServices]] = {
    getMeta(resolveMetaUri(fileId, version))
  }

  private def getMeta(uri: Uri): Future[Option[FileMetadataWithServices]] = {
    val request = Get(uri)
    (for {
      response <- Http().singleRequest(request)
      responseEntity <- response.entity.toStrict(5.seconds)
      _ = logger.debug(s"Received 'getMeta' response '${responseEntity.data.utf8String}'")
      response <- Unmarshal(responseEntity).to[FileMetadataWithServices]
    } yield Some(response))
      .recoverWith { case e: Throwable =>
        logger.error("Couldn't get remote meta", e)
        Future.successful(None)
      }
  }

  private def resolveMetaUri(fileId: String): Uri =
    Uri(s"/meta/$fileId").resolvedAgainst(syncHostUri)

  private def resolveMetaUri(fileId: String, version: String): Uri =
    Uri(s"/meta/$fileId/$version").resolvedAgainst(syncHostUri)

}
