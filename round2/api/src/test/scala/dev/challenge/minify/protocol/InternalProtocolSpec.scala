package dev.challenge.minify.protocol

import dev.challenge.minify.dto.internal.{ErrorResponse, RedirectResponse, SuccessResponse}
import dev.challenge.minify.util.BaseSpec
import spray.json._

class InternalProtocolSpec extends BaseSpec with InternalProtocol {

  "minifyResponseReader" should "read redirect response" in {
    val json =
      """
        |{
        | "url": "url1",
        | "success": true,
        | "redirect": true
        |}
      """.stripMargin.parseJson

    minifyResponseReader.read(json) should be(RedirectResponse("url1"))
  }

  it should "read success response" in {
    val json =
      """
        |{
        | "url": "url1",
        | "success": true,
        | "css": "css1"
        |}
      """.stripMargin.parseJson

    minifyResponseReader.read(json) should be(SuccessResponse("url1", "css1"))
  }

  it should "read error response" in {
    val json =
      """
        |{
        | "success": false,
        | "url": "url1",
        | "error": "something"
        |}
      """.stripMargin.parseJson

    minifyResponseReader.read(json) should be(ErrorResponse("url1", "something"))
  }

}
