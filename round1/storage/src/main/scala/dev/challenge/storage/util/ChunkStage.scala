package dev.challenge.storage.util

import akka.stream.stage._
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.util.ByteString

class ChunkStage(chunkSize: Int) extends GraphStage[FlowShape[ByteString, ByteString]] {

  val in: Inlet[ByteString] = Inlet[ByteString]("Chunker.in")
  val out: Outlet[ByteString] = Outlet[ByteString]("Chunker.out")

  override val shape: FlowShape[ByteString, ByteString] = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    private var buffer = ByteString.empty

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        if (isClosed(in)) pushChunkOrPull()
        else pull(in)
      }
    })

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val elem = grab(in)
        buffer ++= elem
        pushChunkOrPull()
      }

      override def onUpstreamFinish(): Unit = {
        if (buffer.nonEmpty && isAvailable(out)) pushChunkOrPull()
        else completeStage()
      }
    })

    def pushChunkOrPull(): Unit = {
      if (buffer.length < chunkSize) {
        if (isClosed(in)) {
          push(out, buffer)
          completeStage()
        }
        else pull(in)
      } else {
        val (chunk, nextBuffer) = buffer.splitAt(chunkSize)
        buffer = nextBuffer
        push(out, chunk)
      }
    }

  }

}
