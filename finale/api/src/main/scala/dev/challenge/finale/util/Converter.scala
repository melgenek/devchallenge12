package dev.challenge.finale.util

import dev.challenge.finale.dto.CreateValueRequest
import dev.challenge.finale.model.ValueModel

object Converter {

  def requestToModel(request: CreateValueRequest): ValueModel =
    ValueModel(request.value, request.author)

}
