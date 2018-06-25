import scala.sys.process._

docker := Def.task {
  (s"docker build -t devchallenge/processor:latest ${baseDirectory.value.getCanonicalPath}" !)
  ImageId("devchallenge/processor:latest")
}.value
