package dev.challenge.minify.util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.{Delete, Post}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.{ActorMaterializer, Materializer}
import dev.challenge.minify.data.{UrlCssData, UrlData}
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

object HttpClient extends DefaultJsonProtocol {

  private implicit val actorSystem: ActorSystem = ActorSystem("e2e")
  private implicit val actorMaterializer: Materializer = ActorMaterializer()(actorSystem)
  private implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  def minify(urls: Seq[UrlData]): Future[Seq[UrlCssData]] = {
    val seq: JsValue = JsArray(urls.map(_.url).map(e => JsString(e)).toVector)
    val entity = HttpEntity(ContentTypes.`application/json`, JsObject(("urls", seq)).toString())
    val request = Post(s"$apiUri/minify", entity)
    Http().singleRequest(request)
      .flatMap(_.entity.toStrict(30.seconds))
      .map(_.data.utf8String.parseJson.asJsObject)
      .map(_.fields.map { case (key, value) => UrlCssData(key, value.convertTo[String]) }.toSeq)
  }

  def clearAll(): Future[Unit] = {
    Http().singleRequest(Delete(s"$apiUri/cache")).map(_ => ())
  }

//  def apiUri = s"http://${ComposeContainers.apiHost()}:${ComposeContainers.apiPort()}"
  def apiUri = s"http://localhost:8080"

}
