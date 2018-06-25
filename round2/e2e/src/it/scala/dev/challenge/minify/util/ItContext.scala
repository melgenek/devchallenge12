package dev.challenge.minify.util

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

object ItContext {
  private val executionContext = ExecutionContext.fromExecutor(Executors.newWorkStealingPool())

}

trait ItContext {
  implicit val executionContext: ExecutionContext = ItContext.executionContext
}
