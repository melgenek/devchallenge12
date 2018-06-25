package dev.challenge.storage

import dev.challenge.storage.config.{ControllerModule, QueueModule}

trait Wiring extends ControllerModule with QueueModule
