package dev.challenge.finale.util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.{Get, Put, Delete}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.scalalogging.StrictLogging
import dev.challenge.finale.dto.ValueDto
import io.circe.generic.auto._
import io.circe.parser.decode

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

object HttpClient extends StrictLogging {

  private implicit val actorSystem: ActorSystem = ActorSystem("e2e")
  private implicit val actorMaterializer: Materializer = ActorMaterializer()(actorSystem)
  private implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  def save(uri: String, name: String, value: String): Future[Unit] = {
    val entity = HttpEntity(ContentTypes.`application/json`,
      s"""
         |{
         |"value" : "$value",
         |"author" : "any"
         |}""".stripMargin)
    val request = Put(s"$uri/records/$name", entity)
    Http().singleRequest(request)
      .flatMap(_.entity.toStrict(30.seconds))
      .map(r => logger.info(r.data.utf8String))
  }

  def get(uri: String, name: String): Future[Option[String]] = {
    val request = Get(s"$uri/records/$name")
    Http().singleRequest(request)
      .flatMap(_.entity.toStrict(30.seconds))
      .map { b =>
        logger.info(b.data.utf8String)
        b
      }
      .map(b => decode[ValueDto](b.data.utf8String).toOption.map(_.value))
  }

  def delete(uri: String, name: String): Future[Unit] = {
    val request = Delete(s"$uri/records/$name")
    Http().singleRequest(request)
      .flatMap(_.entity.toStrict(30.seconds))
      .map(b => logger.info(s"$request ::" + b.data.utf8String))
  }

}
