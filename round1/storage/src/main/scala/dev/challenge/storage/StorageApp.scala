package dev.challenge.storage

import akka.http.scaladsl.Http
import akka.http.scaladsl.util.FastFuture._
import com.typesafe.scalalogging.StrictLogging

class StorageApp extends Wiring with StrictLogging {

  private val httpInterface: String = config.getString("http.interface")
  private val httpPort: Int = config.getInt("http.port")

  def run(): Unit = {
    Http().bindAndHandle(routes, httpInterface, httpPort).fast
      .map(binding => logger.info(s"Storage server started on ${binding.localAddress}"))
    ()
  }

}

object StorageApp extends StrictLogging {

  def main(args: Array[String]): Unit = {
    try {
      val app = new StorageApp
      app.run()
      logger.info("Application stated")
    } catch {
      case e: Throwable =>
        logger.error("Couldn't start application", e)
        System.exit(1)
    }
  }

}
