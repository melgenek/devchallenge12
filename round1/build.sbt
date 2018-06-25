scalaVersion := "2.12.6"

lazy val dev_challenge = (project in file("."))
  .aggregate(
    storage,
    sync,
    e2e,
    common
  )

val common = project
val e2e = project.dependsOn(common)
val sync = project.dependsOn(common)
val storage = project.dependsOn(common)

