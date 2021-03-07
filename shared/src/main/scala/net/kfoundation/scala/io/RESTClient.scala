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
import net.kfoundation.scala.serialization._

import scala.concurrent.{ExecutionContext, Future}



object RESTClient {
  class Request[T](val method: UString, val url: URL,
    val headers: Map[UString, UString], val body: T)

  class Response[T](val status: Int, val headers: Map[UString, UString],
    val body: Option[T], val error: Option[UString])

  private def mapResponse[S](rs: WebClient.Response,
      factory: ObjectDeserializerFactory, reader: ValueReader[S]):
      Response[S] =
    if(rs.status >= 200 && rs.status <= 300) {
      new Response(rs.status, rs.headers,
        Some(reader.read(factory, UString.of(rs.body))), None)
    } else {
      new Response[S](rs.status, rs.headers, None, Some(UString.of(rs.body)))
    }
}



/**
 * Uses Universal Serialization to perform REST operations on entities on
 * a web service.
 */
class RESTClient(
    driver: WebClient,
    serializer: ObjectBiFactory)(
    implicit ec: ExecutionContext)
  extends WebClient
{
  import RESTClient._
  import ValueReadWriters.NOTHING

  /** Delegates the given request to the underlying WebClient. */
  override def perform(request: WebClient.Request): Future[WebClient.Response] =
    driver.perform(request)


  /**
   * Performs the given request. Value reader and writer for the request
   * entity type should be provided.
   */
  def perform[R, S](request: Request[R])(implicit writer: ValueWriter[R],
      reader: ValueReader[S]): Future[Response[S]] =
    driver.perform(new WebClient.Request(request.method, request.url,
        request.headers, writer.toString(serializer, request.body).toUtf8))
      .map(mapResponse(_, serializer, reader))


  /** Performs a GET operation. */
  def get[S](url: URL)(implicit reader: ValueReader[S]): Future[Response[S]] =
    perform(new Request(WebClient.GET, url, Map.empty, ()))


  /** Performs a POST operation. */
  def post[R, S](url: URL, body: R)(implicit writer: ValueWriter[R],
      reader: ValueReader[S]): Future[Response[S]] =
    perform(new Request(WebClient.POST, url, Map.empty, body))


  /** Performs a PUT operation. */
  def put[R, S](url: URL, body: R)(implicit writer: ValueWriter[R],
      reader: ValueReader[S]): Future[Response[S]] =
    perform(new Request(WebClient.PUT, url, Map.empty, body))


  /** Performs a DELETE operation. */
  def delete[S](url: URL)(implicit reader: ValueReader[S]): Future[Response[S]] =
    perform(new Request(WebClient.DELETE, url, Map.empty, ()))
}