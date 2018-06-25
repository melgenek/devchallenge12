package dev.challenge.storage

import dev.challenge.storage.config.{ControllerModule, DiscoveryModule}

trait Wiring extends ControllerModule with DiscoveryModule
