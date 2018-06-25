package dev.challenge.finale


import akka.http.scaladsl.Http
import akka.http.scaladsl.util.FastFuture._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class FinaleApp extends Wiring with StrictLogging {

  private val httpInterface: String = config.getString("http.interface")
  private val httpPort: Int = config.getInt("http.port")

  def run(): Unit = {
    eventListener.listen(eventService.handle)
    val httpFuture: Future[Unit] = Http().bindAndHandle(routes, httpInterface, httpPort).fast
      .map(binding => logger.info(s"Finale server started on ${binding.localAddress}"))
    Await.result(httpFuture, 30.seconds)
  }

}

object FinaleApp extends StrictLogging {

  def main(args: Array[String]): Unit = {
    try {
      val app = new FinaleApp
      app.run()
      logger.info("Application stated")
    } catch {
      case e: Throwable =>
        logger.error("Couldn't start application", e)
        System.exit(1)
    }
  }

}
