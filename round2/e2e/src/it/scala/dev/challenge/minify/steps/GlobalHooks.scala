package dev.challenge.minify.steps

import cucumber.api.scala.ScalaDsl
import dev.challenge.minify.util.ComposeContainers

class GlobalHooks extends ScalaDsl {

  Before { sc =>
    println("Running: " + sc.getId + "  " + sc.getName)
    if (!ComposeContainers.containersStarted) {
      ComposeContainers.container.starting(ComposeContainers.suiteDescription)

      Runtime.getRuntime.addShutdownHook(new Thread() {
        override def run(): Unit = {
          ComposeContainers.container.finished(ComposeContainers.suiteDescription)
        }
      })
      ComposeContainers.containersStarted = true
    }
  }

}
