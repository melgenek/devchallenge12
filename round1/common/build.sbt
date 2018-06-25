val versions = new {
  val akkaHttp = "10.0.10"
}

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % versions.akkaHttp,
  "com.typesafe.akka" %% "akka-http-spray-json" % versions.akkaHttp,

  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "0.19",

  "com.orbitz.consul" % "consul-client" % "1.1.2",
  "com.aerospike" % "aerospike-client" % "4.1.2",

  // logging
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
)
