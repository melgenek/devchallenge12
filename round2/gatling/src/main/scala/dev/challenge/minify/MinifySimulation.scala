package dev.challenge.minify


import io.gatling.app.Gatling
import io.gatling.core.Predef._
import io.gatling.core.config.GatlingPropertiesBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._


class MinifySimulation extends Simulation {

  val httpConf: HttpProtocolBuilder = http.baseURL("http://localhost:8080")

  val scn: ScenarioBuilder = scenario("HelloSimulation")
    .exec(
      http("minify_1")
        .post("/minify")
        .body(StringBody("""{"urls":["http://nginx:8081/page1/"]}""")).asJSON
        .check(substring("""http://nginx:8081/page1/"""))
    )
    .pause(3.seconds)

  setUp(
    scn.inject(
      constantUsersPerSec(10) during 10.seconds
    )
  ).protocols(httpConf)
}


object MinifySimulation {

  def main(args: Array[String]): Unit = {
    val simClass = classOf[MinifySimulation].getName
    val props = new GatlingPropertiesBuilder
    props.simulationClass(simClass)
    Gatling.fromMap(props.build)
  }

}
