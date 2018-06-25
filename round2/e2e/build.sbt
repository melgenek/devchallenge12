import scala.sys.process._

configs(IntegrationTest)
Defaults.itSettings

libraryDependencies ++= Seq(
  "io.cucumber" % "cucumber-core" % "2.0.1",
  "io.cucumber" %% "cucumber-scala" % "2.0.1",
  "io.cucumber" % "cucumber-jvm" % "2.0.1",
  "io.cucumber" % "cucumber-junit" % "2.0.1",
  "com.waioeka.sbt" %% "cucumber-runner" % "0.1.5",
  "org.scalatest" %% "scalatest" % "3.0.5",
  "org.testcontainers" % "testcontainers" % "1.7.3",

  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10",

  // logging
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
).map(_ % "it")


val cucumberFramework = new TestFramework("com.waioeka.sbt.runner.CucumberFramework")
testFrameworks in IntegrationTest += cucumberFramework
unmanagedClasspath in IntegrationTest += baseDirectory.value / "src/it/features"

parallelExecution in IntegrationTest := false


docker := Def.task {
  (s"docker build -t devchallenge/static:latest ${baseDirectory.value.getCanonicalPath}" !)
  ImageId("devchallenge/static:latest")
}.value



