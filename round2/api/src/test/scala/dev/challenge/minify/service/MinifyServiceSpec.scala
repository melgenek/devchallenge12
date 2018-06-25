package dev.challenge.minify.service

import dev.challenge.minify.dto.external.{MinifyResponse, Redirect, UrlCss}
import dev.challenge.minify.util.{BaseSpec, TestContext}
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.{never, verify, when}

import scala.concurrent.Future
import scala.concurrent.duration._

class MinifyServiceSpec extends BaseSpec with TestContext {

  "minify" should "cache successful response" in new Wiring {
    val processorResponse: UrlCss = UrlCss(url1, css1)
    when(processorService.process(url1)).thenReturnAsync(Some(processorResponse))

    val result: MinifyResponse = service.minify(Set(url1)).futureValue

    result.cssList should be(Set(UrlCss(url1, css1)))
    verify(cacheService).save(processorResponse)
  }

  it should "not cache empty response" in new Wiring {
    when(processorService.process(url1)).thenReturnAsync(None)

    val result: MinifyResponse = service.minify(Set(url1)).futureValue

    result.cssList should be(Set.empty)
    verify(cacheService, never()).save(any())
  }

  it should "return cached response" in new Wiring {
    val cachedResponse = UrlCss(url1, css1)
    when(cacheService.find(url1)).thenReturnAsync(Some(cachedResponse))

    val result: MinifyResponse = service.minify(Set(url1)).futureValue

    result.cssList should be(Set(UrlCss(url1, css1)))
    verify(processorService, never()).process(any())
  }

  it should "return nothing when processing takes too long" in new Wiring {
    when(processorService.process(url1)).thenReturn(Future {
      Thread.sleep(3.seconds.toMillis)
      Some(UrlCss(url1, css1))
    })

    val result: MinifyResponse = service.minify(Set(url1)).futureValue

    result.cssList should be(Set.empty)
  }

  it should "process multiple urls" in new Wiring {
    when(processorService.process(url1)).thenReturnAsync(Some(UrlCss(url1, css1)))
    when(processorService.process(url2)).thenReturnAsync(Some(UrlCss(url2, css2)))
    when(processorService.process(url3)).thenReturnAsync(Some(Redirect(url3)))

    val result: MinifyResponse = service.minify(Set(url2, url1, url3)).futureValue

    result.cssList should contain allOf(UrlCss(url1, css1), UrlCss(url2, css2))
  }

  it should "not use cache when ttl is 0" in new Wiring {
    val serviceNoCache = new MinifyServiceImpl(processorService, cacheService, 100.millis, storeToCache = false)
    when(processorService.process(url1)).thenReturnAsync(Some(UrlCss(url1, css1)))

    val result: MinifyResponse = serviceNoCache.minify(Set(url1)).futureValue

    result.cssList should be(Set(UrlCss(url1, css1)))
    verify(cacheService, never()).save(any())
    verify(cacheService, never()).find(anyString())
  }

  private trait Wiring {
    val url1 = "any_url1"
    val url2 = "any_url2"
    val url3 = "any_url3"
    val css1 = "some_css1"
    val css2 = "some_css2"

    val processorService: ProcessorService = mock[ProcessorService]

    val cacheService: CssCacheService = mock[CssCacheService]
    when(cacheService.save(any())).thenReturnAsync(())
    when(cacheService.find(anyString())).thenReturnAsync(None)

    val service = new MinifyServiceImpl(processorService, cacheService, 100.millis, true)
  }


}
