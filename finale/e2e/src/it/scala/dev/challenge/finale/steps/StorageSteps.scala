package dev.challenge.finale.steps

import cucumber.api.scala.{EN, ScalaDsl}
import dev.challenge.finale.util.{ComposeContainers, HttpClient}
import org.scalatest.Matchers

import scala.concurrent.Await
import scala.concurrent.duration._

class StorageSteps extends ScalaDsl with EN with Matchers {

  Given("""^user stores name="([^"]*)" and value="([^"]*)" in service "([^"]*)"$""") {
    (name: String, value: String, service: String) =>
      val uri: String = ComposeContainers.apiUri(service)

      Await.result(HttpClient.save(uri, name, value), 30.seconds)
  }

  Then("""^the value for name="([^"]*)" in service "([^"]*)" equals "([^"]*)"$""") {
    (name: String, service: String, expectedValue: String) =>
      val uri: String = ComposeContainers.apiUri(service)
      val res: Option[String] = Await.result(HttpClient.get(uri, name), 30.seconds)

      res should equal(Some(expectedValue))
  }

  Then("""^the value for name="([^"]*)" in service "([^"]*)" is empty$""") {
    (name: String, service: String) =>
      val uri: String = ComposeContainers.apiUri(service)
      val res: Option[String] = Await.result(HttpClient.get(uri, name), 30.seconds)

      res should equal(None)
  }

  Given("""^user deletes name="([^"]*)" in service "([^"]*)"$""") {
    (name: String, service: String) =>
      val uri: String = ComposeContainers.apiUri(service)

      Await.result(HttpClient.delete(uri, name), 30.seconds)
  }

  And("""^user waits '(\d+)' seconds$""") { waitTime: Int =>
    Thread.sleep(waitTime * 1000)
  }

}
