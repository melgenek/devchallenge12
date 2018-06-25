package dev.challenge.finale.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Put
import akka.stream.Materializer
import com.google.common.net.HostAndPort
import com.orbitz.consul.{Consul, HealthClient}
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import dev.challenge.finale.config.ConsulConfig
import dev.challenge.finale.model.ValueModel
import dev.challenge.finale.protocol.ModelProtocol

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

trait InternalService {

  def storeSynchronously(name: String, model: ValueModel): Future[Unit]

}

class InternalServiceImpl(consulConfig: ConsulConfig)
                       (implicit executionContext: ExecutionContext,
                        materializer: Materializer,
                        actorSystem: ActorSystem)
  extends InternalService with StrictLogging with ModelProtocol with FailFastCirceSupport {

  val consul: Consul = Consul.builder
    .withHostAndPort(HostAndPort.fromParts(consulConfig.host, consulConfig.port))
    .withPing(false)
    .build()

  private val healthClient: HealthClient = consul.healthClient()

  override def storeSynchronously(name: String, model: ValueModel): Future[Unit] = {
    val healthyPeers: Seq[(String, Int)] = healthClient.getHealthyServiceInstances(consulConfig.serviceName)
      .getResponse.asScala
      .map(health => (health.getService.getAddress, health.getService.getPort))

    val results: Seq[Future[Unit]] = healthyPeers.map { case (host, port) =>
      val request = Put(s"http://$host:$port/records/$name/internal", model)
      Http().singleRequest(request)
        .flatMap(_.entity.toStrict(30.seconds))
        .map(r => logger.info(s"Synchronously replicated $model :: ${r.data.utf8String}"))
        .recover { case e =>
          logger.info(s"Could not Synchronously replicate $model :: ${e.getMessage}")
        }
    }

    Future.successful(results).map(_ => ())
  }

}
