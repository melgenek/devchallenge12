package dev.challenge.storage

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString
import cucumber.api.scala.{EN, ScalaDsl}
import dev.challenge.storage.client.InternalFileClient
import org.scalatest.Matchers

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

class StorageSteps extends ScalaDsl with EN with Matchers {


  implicit val actorSystem: ActorSystem = ActorSystem("cucumber")
  implicit val actorMaterializer: Materializer = ActorMaterializer()(actorSystem)
  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  private val internalClient = new InternalFileClient()

  var file: ByteString = _

  var result: String = _

  Given("""^user creates a file with text "([^"]*)"$""") { text: String =>
    file = ByteString(text)
  }

  Given("""^uploads this file to "([^"]*)" with id "([^"]*)" and version "([^"]*)"$""") { (uri: String, fileId: String, version: String) =>
    val future = internalClient.uploadFile(uri, fileId, version, "someFile", ContentTypes.`text/plain(UTF-8)`, Source.single(file))
    Await.result(future, Duration.Inf)
  }

  And("""^user drinks coffee for a while$""") { () =>
    Thread.sleep(2000)
  }

  When("""^user downloads file "([^"]*)" from "([^"]*)"$""") { (fileId: String, uri: String) =>
    val future = internalClient.downloadFile(uri, fileId).runWith(Sink.seq)
    val byteStings = Await.result(future, Duration.Inf)
    val res = byteStings.fold(ByteString.empty)((acc, str) => acc ++ str)
    result = res.utf8String
  }

  When("""^user downloads file "([^"]*)" with version "([^"]*)" from "([^"]*)"$""") { (fileId: String, version: String, uri: String) =>
    val future = internalClient.downloadFile(uri, fileId, version).runWith(Sink.seq)
    val byteStings = Await.result(future, Duration.Inf)
    val res = byteStings.fold(ByteString.empty)((acc, str) => acc ++ str)
    result = res.utf8String
  }

  Then("""^file contains "([^"]*)"$""") { expectedResult: String =>
    result should equal(expectedResult)
    result = null
  }

}
