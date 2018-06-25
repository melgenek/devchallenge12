libraryDependencies ++= Seq(
  "io.cucumber" % "cucumber-core" % "2.0.1" % Test,
  "io.cucumber" %% "cucumber-scala" % "2.0.1" % Test,
  "io.cucumber" % "cucumber-jvm" % "2.0.1" % Test,
  "io.cucumber" % "cucumber-junit" % "2.0.1" % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

enablePlugins(CucumberPlugin)

CucumberPlugin.glue := "dev/challenge/storage/"

def before(): Unit = {
  println("beforeAll")
}

def after(): Unit = {
  println("afterAll")
}

CucumberPlugin.beforeAll := before
CucumberPlugin.afterAll := after
