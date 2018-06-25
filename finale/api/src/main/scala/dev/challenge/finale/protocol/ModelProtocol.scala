package dev.challenge.finale.protocol

import java.time.ZonedDateTime

import cats.syntax.either._
import dev.challenge.finale.model.ValueModel
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

trait ModelProtocol {

  implicit val encodeInstant: Encoder[ZonedDateTime] = Encoder.encodeString.contramap[ZonedDateTime](_.toString)

  implicit val decodeInstant: Decoder[ZonedDateTime] = Decoder.decodeString.emap { str =>
    Either.catchNonFatal(ZonedDateTime.parse(str)).leftMap(t => "Wrong ZonedDateTime format")
  }

  implicit val valueModelEncoder: Encoder[ValueModel] = deriveEncoder[ValueModel]
  implicit val valueModelDecoder: Decoder[ValueModel] = deriveDecoder[ValueModel]


}
