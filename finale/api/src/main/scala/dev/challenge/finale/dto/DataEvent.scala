package dev.challenge.finale.dto

import dev.challenge.finale.model.ValueModel

abstract class DataEvent(val eventType: String)

case class UpdateValue(name: String, model: ValueModel) extends DataEvent(EventTypes.Update)

case class DeleteValue(name: String) extends DataEvent(EventTypes.Delete)

object EventTypes {
  val Update = "update_value"
  val Delete = "delete_value"
}
