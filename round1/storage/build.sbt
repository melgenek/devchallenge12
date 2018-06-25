name := "storage"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.325"
)

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
