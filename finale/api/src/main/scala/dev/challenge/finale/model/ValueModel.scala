package dev.challenge.finale.model

import java.time.ZonedDateTime
import java.util.UUID

case class ValueModel(value: String,
                      author: String,
                      id: UUID = UUID.randomUUID(),
                      createdAt: ZonedDateTime = ZonedDateTime.now())
