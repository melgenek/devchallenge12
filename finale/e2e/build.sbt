import Dependencies._

configs(IntegrationTest)
Defaults.itSettings

libraryDependencies ++= Seq(
  akka.http,
  circe,
  macwire,
  logging,
  cucumber
).flatten

val cucumberFramework = new TestFramework("com.waioeka.sbt.runner.CucumberFramework")
testFrameworks in IntegrationTest += cucumberFramework
unmanagedClasspath in IntegrationTest += baseDirectory.value / "src/it/features"

parallelExecution in IntegrationTest := false
