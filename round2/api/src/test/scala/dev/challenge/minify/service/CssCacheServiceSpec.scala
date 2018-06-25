package dev.challenge.minify.service

import dev.challenge.minify.cache.AsyncCache
import dev.challenge.minify.dto.external.{Redirect, UrlCss, UrlResponse}
import dev.challenge.minify.util.{BaseSpec, TestContext}
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.{never, verify, when}

import scala.concurrent.duration._

class CssCacheServiceSpec extends BaseSpec with TestContext {

  "save" should "save url css response to specific cache" in new Wiring {
    service.save(UrlCss(url, css)).futureValue

    verify(cssCache).put(url, css, ttl)
    verify(redirectCache, never()).put(anyString(), anyString(), any())
  }

  it should "save redirect response to specific cache" in new Wiring {
    service.save(Redirect(url)).futureValue

    verify(redirectCache).put(url, url, ttl)
    verify(cssCache, never()).put(anyString(), anyString(), any())
  }

  "find" should "return css response when cached" in new Wiring {
    when(cssCache.find(url)).thenReturnAsync(Some(css))

    val result: Option[UrlResponse] = service.find(url).futureValue

    result should be(Some(UrlCss(url, css)))
  }

  it should "return redirect response when cached" in new Wiring {
    when(redirectCache.find(url)).thenReturnAsync(Some(url))

    val result: Option[UrlResponse] = service.find(url).futureValue

    result should be(Some(Redirect(url)))
  }

  it should "return nothing when no cache values" in new Wiring {
    val result: Option[UrlResponse] = service.find(url).futureValue

    result should be(None)
  }

  "delete" should "delete url from css and redirect caches" in new Wiring {
    service.delete(url).futureValue

    verify(cssCache).delete(url)
    verify(redirectCache).delete(url)
  }

  "deleteAll" should "delete all urls from css and redirect caches" in new Wiring {
    service.deleteAll().futureValue

    verify(cssCache).deleteAll()
    verify(redirectCache).deleteAll()
  }

  private trait Wiring {
    val url = "url"
    val css = "css"
    val ttl: FiniteDuration = 100.seconds

    val cssCache: AsyncCache[String, String] = mock[AsyncCache[String, String]]
    when(cssCache.put(anyString(), anyString(), any())).thenReturnAsync(())
    when(cssCache.find(anyString())).thenReturnAsync(None)
    when(cssCache.delete(anyString())).thenReturnAsync(())
    when(cssCache.deleteAll()).thenReturnAsync(())

    val redirectCache: AsyncCache[String, String] = mock[AsyncCache[String, String]]
    when(redirectCache.put(anyString(), anyString(), any())).thenReturnAsync(())
    when(redirectCache.find(anyString())).thenReturnAsync(None)
    when(redirectCache.delete(anyString())).thenReturnAsync(())
    when(redirectCache.deleteAll()).thenReturnAsync(())

    val service = new CssCacheServiceImpl(cssCache, redirectCache, ttl)
  }

}
