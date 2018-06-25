package dev.challenge.minify.dto.external

case class MinifyResponse(cssList: Set[UrlCss])

trait UrlResponse {
  def url: String
}

case class UrlCss(url: String, css: String) extends UrlResponse

case class Redirect(url: String) extends UrlResponse

