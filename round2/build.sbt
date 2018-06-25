scalaVersion := "2.12.6"

lazy val dev_challenge = (project in file("."))
  .aggregate(
    api,
    processor,
    e2e,
    gatling
  )

val api = project
val processor = project
val e2e = project
val gatling = project
