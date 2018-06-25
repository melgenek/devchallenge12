package dev.challenge.finale.util

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._

class BaseSpec extends FlatSpecLike with Matchers with ScalaFutures with MockitoSugar {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(5.seconds, 100.millis)

}
