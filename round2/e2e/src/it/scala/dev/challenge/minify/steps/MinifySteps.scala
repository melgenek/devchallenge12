package dev.challenge.minify.steps

import java.util

import cucumber.api.scala.{EN, ScalaDsl}
import dev.challenge.minify.data.{UrlCssData, UrlData}
import dev.challenge.minify.util.HttpClient
import org.scalatest.Matchers

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

class MinifySteps extends ScalaDsl with EN with Matchers {

  var minifyResponse: Seq[UrlCssData] = null

  Given("""^user requests to minify:$""") { urls: util.List[UrlData] =>
    minifyResponse = Await.result(HttpClient.minify(urls.asScala), 30.seconds)
  }

  Then("""^response contains css for pages:$""") { urls: util.List[UrlCssData] =>
    minifyResponse.toSet should equal(urls.asScala.toSet)
  }

  And("""^response contains no pages$""") { () =>
    minifyResponse should have size 0
  }

  And("""^user waits '(\d+)' seconds$""") { waitTime: Int =>
    Thread.sleep(waitTime * 1000)
  }

  And("""^user clears the cache$""") { () =>
    Await.result(HttpClient.clearAll(), 30.seconds)
  }

}
