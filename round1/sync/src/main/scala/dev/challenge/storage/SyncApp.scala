package dev.challenge.storage


import akka.http.scaladsl.Http
import akka.http.scaladsl.util.FastFuture._
import com.typesafe.scalalogging.StrictLogging

class SyncApp extends Wiring with StrictLogging {

  private val httpInterface: String = config.getString("http.interface")
  private val httpPort: Int = config.getInt("http.port")

  def run(): Unit = {
    syncQueueListener.listen(syncService.sync).fast
      .map(_ => logger.info(s"Sync listener started"))
    Http().bindAndHandle(routes, httpInterface, httpPort).fast
      .map(binding => logger.info(s"Storage server started on ${binding.localAddress}"))
    ()
  }

}

object SyncApp extends StrictLogging {

  def main(args: Array[String]): Unit = {
    try {
      val app = new SyncApp
      app.run()
      logger.info("Application stated")
    } catch {
      case e: Throwable =>
        logger.error("Couldn't start application", e)
        System.exit(1)
    }
  }

}
