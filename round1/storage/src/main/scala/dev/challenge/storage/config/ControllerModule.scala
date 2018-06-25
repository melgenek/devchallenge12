package dev.challenge.storage.config

import akka.http.scaladsl.server.{Directives, Route}
import dev.challenge.storage.controller.{DownloadController, IndexController, InternalUploadController, UploadController}
import dev.challenge.storage.util.AbstractController

trait ControllerModule extends ServiceModule with Directives {

  val internalUploadController = new InternalUploadController(uploadService)

  val uploadController: UploadController = new UploadController(uploadService, notificationService)

  val downloadController: DownloadController = new DownloadController(downloadService)

  val indexController: IndexController = new IndexController()

  val controllers: Set[AbstractController] = Set(
    indexController,
    internalUploadController,
    uploadController,
    downloadController
  )

  val routes: Route = controllers.foldLeft[Route](reject)(_ ~ _.route)

}
