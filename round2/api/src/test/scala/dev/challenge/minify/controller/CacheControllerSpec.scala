package dev.challenge.minify.controller

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import dev.challenge.minify.service.CssCacheService
import dev.challenge.minify.util.BaseSpec
import org.mockito.Matchers.anyString
import org.mockito.Mockito.{never, verify, when}

class CacheControllerSpec extends BaseSpec with ScalatestRouteTest {

  "/cache" should "delete single url" in new Wiring {
    val requestBody: String =
      """
        |{
        |  "url": "url1"
        |}
      """.stripMargin
    val request = HttpEntity(ContentTypes.`application/json`, requestBody)

    Delete("/cache", request) ~> controller.route ~> check {
      status should equal(StatusCodes.OK)
      verify(cacheService).delete("url1")
      verify(cacheService, never()).deleteAll()
    }
  }

  it should "delete single url when no url specified" in new Wiring {
    Delete("/cache") ~> controller.route ~> check {
      status should equal(StatusCodes.OK)
      verify(cacheService).deleteAll()
    }
  }

  private trait Wiring {
    val cacheService: CssCacheService = mock[CssCacheService]
    when(cacheService.delete(anyString())).thenReturnAsync(())
    when(cacheService.deleteAll()).thenReturnAsync(())

    val controller = new CacheController(cacheService)
  }


}
