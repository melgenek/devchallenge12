package dev.challenge.minify.dto.internal

sealed trait InternalMinifyResponse

case class SuccessResponse(url: String, css: String) extends InternalMinifyResponse

case class RedirectResponse(url: String) extends InternalMinifyResponse

case class ErrorResponse(url: String, message: String) extends InternalMinifyResponse
