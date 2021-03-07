// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.io

import net.kfoundation.scala.parse.{CodeLocation, ParseError}
import net.kfoundation.scala.parse.lex.{CodeWalker, URLParser}
import net.kfoundation.scala.serialization._
import net.kfoundation.scala.serialization.internals.ObjectStreamStateMachine
import net.kfoundation.scala.util.WQName
import net.kfoundation.scala.{UChar, UObject, UString}



object URL {
  object Authority {
    def apply(host: WQName, port: Option[Int] = None,
      user: Option[UString] = None) = new Authority(host, user, port)
    def apply(domain: Seq[UString]) = new Authority(WQName(domain), None, None)
  }

  final class Authority(val host: WQName, val user: Option[UString],
    val port: Option[Int])
  {
    def withUser(u: UString): Authority =
      new Authority(host, Some(u), port)

    def withPort(p: Int): Authority =
      new Authority(host, user, Some(p))

    override def equals(other: Any): Boolean = other match {
      case that: Authority =>
        host == that.host &&
        user == that.user &&
        port == that.port
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(host, user, port)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }
  }


  final class QueryParam(val key: UString, val value: UString) extends UObject {
    override def appendTo(builder: UString.Builder): Unit =
      builder.appendAll(key, EQ, value)

    override def equals(other: Any): Boolean = other match {
      case that: QueryParam =>
        key == that.key &&
        value == that.value
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(key, value)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }
  }

  object Query {
    def serialize[T](value: T)(implicit writer: ValueWriter[T]): Query = {
      val s = new URLQueryObjectSerializer
      writer.write(s, value)
      new Query(s.get)
    }

    def apply(m: Map[String, Seq[String]]): Query = new Query(
      m.flatMap(kv =>
        if(kv._2.isEmpty) {
          Seq(new QueryParam(kv._1, ""))
        } else {
          kv._2.map(v => new QueryParam(kv._1, v))
        })
        .toSeq)

    def apply(params: (UString, UString)*) = new Query(
      params.map(t => new QueryParam(t._1, t._2)))
  }


  final class Query(val params: Seq[QueryParam]) extends UObject {
    def getAll(key: UString): Seq[UString] =
      params.filter(_.key.equals(key)).map(_.value)

    def get(key: UString): Option[UString] =
      params.find(_.key.equals(key)).map(_.value)

    def add(key: UString, value: UString): Query = new Query(
      params.appended(new QueryParam(key, value)))

    def add(query: Query): Query = new Query(
      params.appendedAll(query.params))

    def add(param: QueryParam) = new Query(params.appended(param))

    def replace(key: UString, value: UString): Query =
      new Query(params.filter(_.key.equals(key))
        .appended(new QueryParam(key, value)))

    def remove(key: UString): Query =
      new Query(params.filter(_.key.equals(key)))

    def isEmpty: Boolean = params.isEmpty

    def parse[T](implicit reader: ValueReader[T]): T = {
      val d = new URLQueryObjectDeserializer(this)
      reader.read(d)
    }

    def toMap: Map[UString, UString] = params.map(p => (p.key, p.value)).toMap

    override def appendTo(builder: UString.Builder): Unit =
      builder.appendJoining(params, AMP)

    override def equals(other: Any): Boolean = other match {
      case that: Query =>
        params == that.params
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(params)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }
  }


  private class URLQueryObjectDeserializer(q: Query) extends ObjectDeserializer {
    private var it = q.params.iterator
    private val stateMachine = new ObjectStreamStateMachine
    private var param: QueryParam = _

    override def select(fields: Seq[UString]): Unit = {
      it = q.params.filter(p => fields.contains(p.key)).iterator
    }
    override def readObjectBegin(): Option[UString] = {
      stateMachine.objectBegin()
      None
    }
    override def readObjectEnd(): Option[UString] = {
      stateMachine.objectEnd()
      None
    }
    override def readCollectionBegin(): Unit =
      throw new DeserializationError("Collections are not supported")
    override def tryReadCollectionEnd(): Boolean =
      throw new DeserializationError("Collections are not supported")
    override def tryReadPropertyName(): Option[UString] =
      if(it.hasNext) {
        stateMachine.property()
        param = it.next()
        Some(param.key)
      } else {
        None
      }
    override def readStringLiteral(): UString = {
      stateMachine.literal()
      param.value
    }
    override def readIntegerLiteral(): Long = {
      stateMachine.literal()
      param.value.toLong
    }
    override def readDecimalLiteral(): Double = {
      stateMachine.literal()
      param.value.toDouble
    }
    override def readBooleanLiteral(): Boolean = {
      stateMachine.literal()
      param.value.isEmpty || param.value.equals(TRUE)
    }
    override protected def getCurrentLocation: CodeLocation =
      new CodeLocation("URLQueryParams")
  }


  private class URLQueryObjectSerializer extends ObjectSerializer {
    private val stateMachine = new ObjectStreamStateMachine
    private var list: Seq[QueryParam] = Seq()
    private var key: UString = _

    def get: Seq[QueryParam] = list

    override def writePropertyName(name: UString): ObjectSerializer = {
      stateMachine.property()
      key = name
      this
    }
    override def writeLiteral(value: UString): ObjectSerializer = {
      stateMachine.literal()
      list = list :+ new QueryParam(key, value)
      this
    }
    override def writeLiteral(value: Long): ObjectSerializer =
      writeLiteral(UString.of(value))
    override def writeLiteral(value: Double): ObjectSerializer =
      writeLiteral(UString.of(value))
    override def writeLiteral(value: Boolean): ObjectSerializer =
      if(value) writeLiteral(TRUE) else writeLiteral(FALSE)
    override def writeNull(): ObjectSerializer =
      writeLiteral(NULL)
    override def writeObjectBegin(name: UString): ObjectSerializer = {
      stateMachine.objectBegin(name)
      this
    }
    override def writeObjectEnd(): ObjectSerializer = {
      stateMachine.objectEnd()
      this
    }
    override def writeCollectionBegin(): ObjectSerializer =
      throw new DeserializationError("Collections are not supported")
    override def writeCollectionEnd(): ObjectSerializer =
      throw new DeserializationError("Collections are not supported")
    override def writeStreamEnd(): Unit = stateMachine.streamEnd()
  }

  private val COLON: UChar = ':'
  private val SLASHES: UString = "//"
  private val AT: UChar = '@'
  private val QUESTION_MARK: UChar = '?'
  private val HASH = '#'
  private val AMP: UChar = '&'
  private val EQ: UChar = '='
  private val TRUE: UString = "true"
  private val FALSE: UString = "false"
  private val NULL: UString = "null"

  val ROOT: Path = Path("/")
  val NO_QUERY = new Query(Seq.empty)


  private def encode(builder: UString.Builder, part: UString,
    isValid: Int => Boolean): Unit =
    part.uCharIterator.foreach(ch => {
      val octets = ch.toUtf8
      if (octets.length == 1 && isValid(octets(0))) {
        builder.append(octets(0).toChar)
      } else {
        octets.foreach(builder.append('%').appendHex(_))
      }
    })


  def encode(str: UString): UString = {
    val b = UString.builder
    encode(b, str, URLParser.isValidQueryChar)
    b.build
  }


  def decode(str: UString): UString =
    URLParser.decode(CodeWalker.of(str))


  def apply(url: UString): URL = {
    val w = CodeWalker.of(url)
    try URLParser.tryRead(w).get catch {
      case e: ParseError => throw new ParseError(
        e.getLocalizableMessage.withParam(ParseError.WAS, url),
        None)
    }
  }


  def apply(scheme: UString, authority: Authority, path: Path = Path.ROOT,
      query: Query = NO_QUERY, fragment: Option[UString] = None) =
    new URL(scheme, authority, path, query, fragment)


  def http(host: UString, path: Path = ROOT, query: Query = NO_QUERY): URL =
    apply("http", Authority(WQName(host)), path, query)


  def https(host: UString, path: Path = ROOT, query: Query = NO_QUERY): URL =
    apply("https", Authority(WQName(host)), path, query)
}


/**
 * Entity object model for building and manipulating URLs.
 */
class URL(val scheme: UString, val authority: URL.Authority, val path: Path,
  val query: URL.Query, val fragment: Option[UString]) extends UObject
{
  import URL._


  override def appendTo(builder: UString.Builder): Unit = builder
    .append(scheme).append(COLON).append(SLASHES)
    .use(b => authority.user.foreach(a => b.append(a).append(AT)))
    .append(authority.host.toUString)
    .use(b => authority.port.foreach(p => b.append(COLON).append(p)))
    .append(path.toUString)
    .use(b => if(!query.isEmpty) {
      b.append(QUESTION_MARK)
        .appendJoining(query.params.map(_.toUString), AMP)
    })
    .use(b => fragment.foreach(f => b.append(HASH).append(f)))


  private def encodePath(builder: UString.Builder): Unit =
    builder.unfold[UString](path.segments, "/",
      (b, s) => encode(b, s, URLParser.isValidPathChar))


  private def encodeQuery(builder: UString.Builder): Unit =
    if(!query.isEmpty) {
      builder.append(QUESTION_MARK)
        .unfold[QueryParam](query.params, "&", (b, p) => {
          b.append(p.key).append('=')
          encode(b, p.value, URLParser.isValidQueryChar)
        })
    }


  /** Returns this URL as a percent-encoded string (RFC 3986 section 2.1). */
  def encoded: UString = UString.builder
    .append(scheme).append(COLON).append(SLASHES)
    .use(b => authority.user.foreach(a => b.append(a).append(AT)))
    .append(authority.host.toUString)
    .use(b => authority.port.foreach(p => b.append(COLON).append(p)))
    .append('/')
    .use(encodePath)
    .use(encodeQuery)
    .use(b => fragment.foreach(f => b.append(HASH).append(f)))
    .build


  /** Replaces the query part of this URL with the one given. */
  def replaceQuery(q: Query): URL =
    new URL(scheme, authority, path, q, fragment)


  /** Adds the given key/value pair to the query part of this URL. */
  def withQuery(key: UString, value: UString): URL = replaceQuery(
    query.add(key, value))


  /**
   * Adds all the fields of the given object to the query part of this URL using
   * `URLQueryObjectSerializer`.
   */
  def withQuery[T](value: T)(implicit writer: ValueWriter[T]): URL = replaceQuery(
    query.add(Query.serialize(value)(writer)))


  /**
   * Alias for withQuery().
   */
  def ?[T](value: T)(implicit writer: ValueWriter[T]): URL = withQuery(value)


  /**
   * Adds the given key-value pair to the query part of this URL.
   */
  def ?(param: (UString, UString)): URL = replaceQuery(query.add(param._1, param._2))

  /** Returns this URL excluding its query part. */
  def withoutQuery: URL = replaceQuery(NO_QUERY)


  /** Adds a segment to the path part of this URL. */
  def subPath(segment: UString) = new URL(scheme, authority,
    path.append(segment), query, fragment)


  /** Appends the segments of the given path to the path part of this URL. */
  def subPath(p: Path) = new URL(scheme, authority, path.append(p),
    query, fragment)


  /** Replaces the path part of this URL with the given path. */
  def withPath(path: Path) = new URL(scheme, authority, path, query, fragment)


  /** Appends a segment to the path part of this URL. */
  def /(segment: UString): URL = subPath(segment)


  /** Appends the segments the given path to the path part of this URL. */
  def /(path: Path): URL = subPath(path)


  /** Returns this URL with one segment of its path removed. */
  def parent: URL = withPath(path.parent)


  /** Replaces the user part of this URL with the one given. */
  def withUser(user: UString) = new URL(scheme, authority.withUser(user), path,
    query, fragment)


  /** Replaces the port part of this URL with the one given. */
  def withPort(port: Int) = new URL(scheme, authority.withPort(port), path,
    query, fragment)


  /** Replaces the fragment part of this URL with the one given. */
  def withFragment(f: UString) = new URL(scheme, authority, path, query,
    Some(f))


  /** Returns this URL with its fragment part removed. */
  def withoutFragment = new URL(scheme, authority, path, query, None)


  override def toString: String = toUString.toString


  override def equals(other: Any): Boolean = other match {
    case that: URL =>
        scheme == that.scheme &&
        authority == that.authority &&
        path == that.path &&
        query == that.query &&
        fragment == that.fragment
    case _ => false
  }


  override def hashCode(): Int = {
    val state = Seq(scheme, authority, path, query, fragment)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
