package dev.challenge.finale.service

import dev.challenge.finale.dto.{DataEvent, DeleteValue, UpdateValue}

trait EventService {

  def handle(event: DataEvent): Unit

}

class EventServiceImpl(storageService: StorageService) extends EventService {
  
  override def handle(event: DataEvent): Unit = event match {
    case UpdateValue(name, value) => storageService.saveIfNotExists(name, value)
    case DeleteValue(name) => storageService.delete(name)
  }

}
