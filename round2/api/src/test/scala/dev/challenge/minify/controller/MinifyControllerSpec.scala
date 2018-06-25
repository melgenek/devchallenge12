package dev.challenge.minify.controller

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import dev.challenge.minify.dto.external.{MinifyResponse, UrlCss}
import dev.challenge.minify.service.MinifyService
import dev.challenge.minify.util.BaseSpec
import org.mockito.Mockito.when
import spray.json._

class MinifyControllerSpec extends BaseSpec with ScalatestRouteTest {

  "/minify" should "return minified css for each url" in new Wiring {
    val response = MinifyResponse(Set(UrlCss("url1", "css1"), UrlCss("url3", "css3")))
    when(minifyService.minify(Set("url1", "url2", "url3"))).thenReturnAsync(response)

    val requestBody: String =
      """
        |{
        |  "urls": [
        |    "url1",
        |    "url2",
        |    "url3"
        |  ]
        |}
      """.stripMargin
    val request = HttpEntity(ContentTypes.`application/json`, requestBody)

    Post("/minify", request) ~> controller.route ~> check {
      status should equal(StatusCodes.OK)
      entityAs[String].parseJson should be(
        """
          |{
          |  "url1": "css1",
          |  "url3": "css3"
          |}
        """.stripMargin.parseJson)
    }
  }

  private trait Wiring {
    val minifyService: MinifyService = mock[MinifyService]

    val controller = new MinifyController(minifyService)
  }

}
