name := "sync"

enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)

dockerfile in docker := {
  val appDir: File = stage.value
  val targetDir = s"/opt/${name.value}"

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
