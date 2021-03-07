// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.io

import net.kfoundation.scala.UString

import scala.concurrent.Future


object WebClient {
  class Request(val method: UString, val url: URL,
    val headers: Map[UString, UString], val body: Array[Byte])

  class Response(val status: Int, val headers: Map[UString, UString],
    val body: Array[Byte])

  class WebClientException(val response: Response) extends
    Exception("Status: " + response.status)

  val POST = "post"
  val PUT = "put"
  val GET = "get"
  val DELETE = "delete"
}


/**
 * Common interface for web client implementations.
 */
trait WebClient {
  import WebClient._
  def perform(request: Request): Future[Response]
}
