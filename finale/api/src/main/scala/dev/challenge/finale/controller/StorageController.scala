package dev.challenge.finale.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import dev.challenge.finale.dto.{CreateValueRequest, DeleteValue, UpdateValue}
import dev.challenge.finale.model.ValueModel
import dev.challenge.finale.protocol.{EventProtocol, ModelProtocol}
import dev.challenge.finale.service.{InternalService, MessageService, StorageService}
import dev.challenge.finale.util.AbstractController
import dev.challenge.finale.util.Converter.requestToModel
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class StorageController(internalService: InternalService,
                        messageService: MessageService,
                        storageService: StorageService)
                       (implicit executionContext: ExecutionContext)
  extends AbstractController with EventProtocol with ModelProtocol {

  override def route: Route =
    pathPrefix("records") {
      pathEndOrSingleSlash {
        getAll
      } ~ pathPrefix(Segment) { name =>
        pathEndOrSingleSlash {
          put {
            saveValue(name)
          } ~ get {
            findValue(name)
          } ~ delete {
            deleteValue(name)
          }
        } ~ (path("history") & get) {
          history(name)
        } ~ (path("internal") & put) {
          store(name)
        }
      }
    }

  protected def getAll: Route = complete(storageService.getAll)

  protected def saveValue(name: String): Route =
    entity(as[CreateValueRequest]) { req =>
      val model: ValueModel = requestToModel(req)
      val saveFuture: Future[Unit] = for {
        _ <- internalService.storeSynchronously(name, model)
        _ <- messageService.send(name, UpdateValue(name, model))
      } yield ()
      onSuccess(saveFuture) {
        complete(StatusCodes.OK)
      }
    }

  protected def findValue(name: String): Route =
    storageService.find(name) match {
      case Some(value) => complete(value)
      case None => complete(StatusCodes.NotFound)
    }

  protected def deleteValue(name: String): Route = {
    complete(messageService.send(name, DeleteValue(name)))
  }

  protected def history(name: String): Route =
    storageService.history(name) match {
      case Some(value) => complete(value)
      case None => complete(StatusCodes.NotFound)
    }

  protected def store(name: String): Route =
    entity(as[ValueModel]) {
      model =>
        complete(storageService.save(name, model))
    }

}
