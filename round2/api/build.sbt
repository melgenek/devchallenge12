name := "api"

configs(IntegrationTest)
Defaults.itSettings


val versions = new {
  val akka = "2.5.12"
  val akkaHttp = "10.0.10"
}

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % versions.akka,
  "com.typesafe.akka" %% "akka-http" % versions.akkaHttp,
  "com.typesafe.akka" %% "akka-http-spray-json" % versions.akkaHttp,

  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "0.19",

  "com.aerospike" % "aerospike-client" % "4.1.2",
  "com.iheart" %% "ficus" % "1.4.3",

  // logging
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "com.typesafe.akka" %% "akka-slf4j" % versions.akka,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
) ++ testDependencies

val testDependencies = Seq(
  "com.typesafe.akka" %% "akka-http-testkit" % versions.akkaHttp,
  "org.scalatest" %% "scalatest" % "3.0.5",
  "org.mockito" % "mockito-core" % "1.10.19",
  "com.dimafeng" %% "testcontainers-scala" % "0.18.0"
).map(_ % "it,test")

enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)

dockerfile in docker := {
  val appDir: File = stage.value
  val targetDir = "/app"

  new Dockerfile {
    from("anapsix/alpine-java:8_server-jre")
    expose(8080)
    copy(appDir, targetDir)
    entryPoint(s"$targetDir/bin/${executableScriptName.value}")
  }
}

imageNames in docker := Seq(
  ImageName(s"devchallenge/${name.value}:latest")
)
