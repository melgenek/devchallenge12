package dev.challenge.minify.service

import dev.challenge.minify.amqp.RpcClient
import dev.challenge.minify.dto.external.{Redirect, UrlCss, UrlResponse}
import dev.challenge.minify.dto.internal.{ErrorResponse, InternalMinifyRequest, InternalMinifyResponse, RedirectResponse, SuccessResponse}
import dev.challenge.minify.util.{BaseSpec, TestContext}
import org.mockito.Mockito.when

class ProcessorServiceSpec extends BaseSpec with TestContext {

  "process" should "return successful response" in new Wiring {
    when(rpcClient.send(InternalMinifyRequest(url))).thenReturnAsync(SuccessResponse(url, css))

    val result: Option[UrlResponse] = service.process(url).futureValue

    result should be(Some(UrlCss(url, css)))
  }

  it should "return redirect response" in new Wiring {
    when(rpcClient.send(InternalMinifyRequest(url))).thenReturnAsync(RedirectResponse(url))

    val result: Option[UrlResponse] = service.process(url).futureValue

    result should be(Some(Redirect(url)))
  }

  it should "not return error response" in new Wiring {
    when(rpcClient.send(InternalMinifyRequest(url))).thenReturnAsync(ErrorResponse(url, "some message"))

    val result: Option[UrlResponse] = service.process(url).futureValue

    result should be(None)
  }

  it should "return empty response when rpc completes with exception" in new Wiring {
    when(rpcClient.send(InternalMinifyRequest(url))).thenFailAsync(new RuntimeException("something went wrong"))

    val result: Option[UrlResponse] = service.process(url).futureValue

    result should be(None)
  }

  private trait Wiring {
    val url = "url"
    val css = "css"

    val rpcClient: RpcClient[InternalMinifyRequest, InternalMinifyResponse] = mock[RpcClient[InternalMinifyRequest, InternalMinifyResponse]]

    val service = new ProcessorServiceImpl(rpcClient)
  }

}
