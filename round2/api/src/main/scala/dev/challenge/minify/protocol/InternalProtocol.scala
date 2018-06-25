package dev.challenge.minify.protocol

import dev.challenge.minify.dto.internal.{ErrorResponse, InternalMinifyRequest, InternalMinifyResponse, RedirectResponse, SuccessResponse}
import spray.json.{DefaultJsonProtocol, RootJsonFormat, RootJsonReader}

trait InternalProtocol extends DefaultJsonProtocol {

  implicit val minifyRequestFormat: RootJsonFormat[InternalMinifyRequest] = jsonFormat1(InternalMinifyRequest)

  implicit val minifyResponseReader: RootJsonReader[InternalMinifyResponse] = json => {
    val success: Boolean = fromField[Boolean](json, "success")
    val url: String = fromField[String](json, "url")
    if (success) {
      val isRedirect: Boolean = fromField[Option[Boolean]](json, "redirect").getOrElse(false)
      if (isRedirect) RedirectResponse(url)
      else {
        val css: String = fromField[String](json, "css")
        SuccessResponse(url, css)
      }
    } else {
      val error: String = fromField[String](json, "error")
      ErrorResponse(url, error)
    }
  }

}

object InternalProtocol extends InternalProtocol
