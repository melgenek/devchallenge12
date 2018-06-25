package dev.challenge.minify

import akka.http.scaladsl.Http
import akka.http.scaladsl.util.FastFuture._
import com.typesafe.scalalogging.StrictLogging
import dev.challenge.minify.config.ControllerModule

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class MinifyApp extends ControllerModule with StrictLogging {

  private val httpInterface: String = config.getString("http.interface")
  private val httpPort: Int = config.getInt("http.port")

  def run(): Unit = {
    val startUpFuture: Future[Unit] = Http().bindAndHandle(routes, httpInterface, httpPort).fast
      .map(binding => logger.info(s"Minify server started on port ${binding.localAddress.getPort}"))
    Await.result(startUpFuture, 30.seconds)
  }

}

object MinifyApp extends StrictLogging {

  def main(args: Array[String]): Unit = {
    try {
      val app = new MinifyApp
      app.run()
      logger.info("Application stated")
    } catch {
      case e: Throwable =>
        logger.error("Couldn't start application", e)
        System.exit(1)
    }
  }

}
