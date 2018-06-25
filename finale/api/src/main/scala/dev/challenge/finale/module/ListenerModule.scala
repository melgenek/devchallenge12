package dev.challenge.finale.module

import com.softwaremill.macwire._
import dev.challenge.finale.dto.DataEvent
import dev.challenge.finale.listener.{EventListener, EventListenerImpl}
import dev.challenge.finale.protocol.EventProtocol

trait ListenerModule extends ActorModule with EventProtocol {

  val eventListener: EventListener[DataEvent] = wire[EventListenerImpl[DataEvent]]

}
