package dev.challenge.minify.util

import akka.actor.ActorSystem
import akka.pattern.after

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.FiniteDuration

object FutureExtensions {

  implicit class TimeoutOps[T](val original: Future[T]) extends AnyVal {
    def withTimeout(value: => T, duration: FiniteDuration)(implicit system: ActorSystem): Future[T] = {
      Future firstCompletedOf Seq(original, after(duration, system.scheduler)(Future.successful(value)))
    }
  }

}