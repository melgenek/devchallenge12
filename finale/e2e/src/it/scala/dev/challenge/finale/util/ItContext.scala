package dev.challenge.finale.util

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

object ItContext {
  private val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(16))

}

trait ItContext {
  implicit val executionContext: ExecutionContext = ItContext.executionContext
}
