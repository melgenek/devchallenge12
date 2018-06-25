package dev.challenge.finale.protocol

import dev.challenge.finale.dto.{DataEvent, DeleteValue, EventTypes, UpdateValue}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, Json}


trait EventProtocol extends ModelProtocol {

  implicit val eventEncoder: Encoder[DataEvent] = event => {
    val json: Json = event match {
      case e: UpdateValue => e.asJson
      case e: DeleteValue => e.asJson
    }
    json.mapObject(_.add("type", Json.fromString(event.eventType)))
  }

  implicit val eventDecoder: Decoder[DataEvent] = (c: HCursor) =>
    for {
      eventType <- c.get[String]("type")
      res <- eventType match {
        case EventTypes.Update => c.value.as[UpdateValue]
        case EventTypes.Delete => c.value.as[DeleteValue]
      }
    } yield res

}

