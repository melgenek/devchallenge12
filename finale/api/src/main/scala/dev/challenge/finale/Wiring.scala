package dev.challenge.finale

import dev.challenge.finale.module.{ControllerModule, ListenerModule}

trait Wiring extends ControllerModule with ListenerModule
