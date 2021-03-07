package net.kfoundation.js.io

import net.kfoundation.scala.UString
import net.kfoundation.scala.io.WebClient
import net.kfoundation.scala.io.WebClient.{Response, WebClientException}
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.{Ajax, AjaxException}

import java.nio.ByteBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.typedarray.{ArrayBuffer, Int8Array}


object JSWebClient {
  import UString.of

  private def parseHeaders(headers: String): Map[UString, UString] =
    headers.split("\r\n")
      .map(line => {
        val parts = line.split(":")
        if(parts.size == 1) {
          (of(parts(0).trim), UString.EMPTY)
        } else {
          (of(parts(0).trim), of(parts(1).trim))
        }
      })
      .toMap

  private def toResponse[T](request: XMLHttpRequest): Response = new Response(
    request.status,
    parseHeaders(request.getAllResponseHeaders()),
    new Int8Array(request.response.asInstanceOf[ArrayBuffer]).toArray)
}


class JSWebClient(implicit ec: ExecutionContext)
  extends WebClient
{
  import JSWebClient._

  override def perform(request: WebClient.Request): Future[Response] =
    Ajax(
      request.method, request.url.encoded,
      if(request.body.isEmpty) null
        else Ajax.InputData.byteBuffer2ajax(ByteBuffer.wrap(request.body)),
      0, request.headers.map(t => (t._1.toString, t._2.toString)), false,
      "arraybuffer")
    .map(toResponse)
    .recover {
      case e: AjaxException => toResponse(e.xhr)
    }
}
