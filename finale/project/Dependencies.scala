import sbt._

object Versions {
  val akka = "2.5.13"
  val akkaHttp = "10.1.3"
  val circe = "0.9.3"
  val slick = "3.2.3"
  val elastic = "6.2.9"
}


object Dependencies {

  object akka {
    val actor = Seq(
      "com.typesafe.akka" %% "akka-actor" % Versions.akka,
      "com.typesafe.akka" %% "akka-testkit" % Versions.akka % Test
    )
    val stream = Seq(
      "com.typesafe.akka" %% "akka-stream" % Versions.akka,
      "com.typesafe.akka" %% "akka-stream-testkit" % Versions.akka % Test
    )
    val http = Seq(
      "com.typesafe.akka" %% "akka-http" % Versions.akkaHttp,
      "com.typesafe.akka" %% "akka-http-testkit" % Versions.akkaHttp % Test
    )
  }

  val circe = Seq(
    "io.circe" %% "circe-core" % Versions.circe,
    "io.circe" %% "circe-generic" % Versions.circe,
    "io.circe" %% "circe-parser" % Versions.circe,
    "de.heikoseeberger" %% "akka-http-circe" % "1.21.0"
  )

  val scalatest = Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % "it, test",
    "org.mockito" % "mockito-core" % "1.10.19" % "it, test",
    "com.dimafeng" %% "testcontainers-scala" % "0.19.0" % "test"
  )

  val macwire = Seq(
    "com.softwaremill.macwire" %% "macros" % "2.3.0" % Provided
  )

  val logging = Seq(
    "org.slf4j" % "slf4j-api" % "1.7.25",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
  )

  val redis = Seq(
    "net.debasishg" %% "redisclient" % "3.7"
  )

  val alpakka = Seq(
    "com.typesafe.akka" %% "akka-stream-kafka" % "0.21.1"
  )

  val cucumber = Seq(
    "io.cucumber" % "cucumber-core" % "2.0.1",
    "io.cucumber" %% "cucumber-scala" % "2.0.1",
    "io.cucumber" % "cucumber-jvm" % "2.0.1",
    "io.cucumber" % "cucumber-junit" % "2.0.1",
    "com.waioeka.sbt" %% "cucumber-runner" % "0.1.5",
    "org.scalatest" %% "scalatest" % "3.0.5",
    "org.testcontainers" % "testcontainers" % "1.8.0"
  )

  val consul = Seq(
    "com.orbitz.consul" % "consul-client" % "1.1.2"
  )

}
