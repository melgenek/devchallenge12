name := "devchallenge_finale"

version := "0.1"

scalaVersion := "2.12.6"

lazy val dev_challenge = (project in file("."))
  .aggregate(
    api,
    e2e
  )

val api = project
val e2e = project
