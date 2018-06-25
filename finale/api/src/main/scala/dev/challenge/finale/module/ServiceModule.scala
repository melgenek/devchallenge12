package dev.challenge.finale.module


import com.softwaremill.macwire._
import dev.challenge.finale.service.{EventService, EventServiceImpl, InternalService, InternalServiceImpl, MessageService, MessageServiceImpl, StorageService, StorageServiceImpl}

trait ServiceModule extends DaoModule {

  val internalService: InternalService = wire[InternalServiceImpl]

  val messageService: MessageService = wire[MessageServiceImpl]

  val storageService: StorageService = wire[StorageServiceImpl]

  val eventService: EventService = wire[EventServiceImpl]

}
