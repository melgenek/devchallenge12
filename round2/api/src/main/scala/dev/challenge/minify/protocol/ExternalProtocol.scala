package dev.challenge.minify.protocol

import dev.challenge.minify.dto.external.{EvictUrlRequest, MinifyRequest, MinifyResponse}
import spray.json.{DefaultJsonProtocol, JsObject, JsString, RootJsonFormat, RootJsonWriter}

trait ExternalProtocol extends DefaultJsonProtocol {

  implicit val minifyRequestFormat: RootJsonFormat[MinifyRequest] = jsonFormat1(MinifyRequest)

  implicit val minifyResponseWriter: RootJsonWriter[MinifyResponse] = (resp: MinifyResponse) => {
    val fields: Seq[(String, JsString)] = resp.cssList.map(urlCss => (urlCss.url, JsString(urlCss.css))).toSeq
    JsObject(fields: _*)
  }

  implicit val evictUrlRequestFormat: RootJsonFormat[EvictUrlRequest] = jsonFormat1(EvictUrlRequest)

  implicit val unitResponseWriter: RootJsonWriter[Unit] = _ => JsString("OK")

}
