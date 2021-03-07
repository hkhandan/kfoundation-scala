package net.kfoundation.scala.parse.lex

import net.kfoundation.scala.UString.Interpolator
import net.kfoundation.scala.io.{Path, URL}
import net.kfoundation.scala.parse.{ParseError, Parser}
import net.kfoundation.scala.util.WQName
import net.kfoundation.scala.{UChar, UString}

import java.io.ByteArrayOutputStream


object URLParser extends Parser[URL] {
  import ParseError._

  private val COLON_CODE = ':'
  private val AT_CODE = '@'
  private val COLON_SLASHES: UString = "://"
  private val QUESTION_MARK = '?'
  private val AMP = '&'
  private val EQ = '='
  private val HASH = '#'
  private val PERCENT = '%'
  private val DASH = '-'

  private val VALID_PATH_CHARS = Array[Int]('-', '.', '_', '~', '!', '$', '&',
    '\'', '(', ')', '*', '+', ',', ';', '=', ':', '@')

  private val VALID_QUERY_CHAR: Array[Char] = Array('-', '_', '.', '!', '~',
    '*', '\'', '(', ')')

  private val PATH_PARSER = Path.parser(ch => ch != '?' && ch != '%' && ch != '#')


  def isValidDomainChar(ch: Int): Boolean =
    UChar.isAlphanumeric(ch) || ch==DASH


  def isValidPathChar(ch: Int): Boolean =
    UChar.isAlphanumeric(ch) || VALID_PATH_CHARS.contains(ch)


  override def tryRead(w: CodeWalker): Option[URL] = {
    val scheme: UString = readScheme(w)
    val authority: URL.Authority = readAuthority(w)
    val path: Path = PATH_PARSER.tryRead(w).get.toAbsolute
    val query: URL.Query = readQuery(w)
    val fragment: Option[UString] = readFragment(w)
    if(w.hasMore) {
      throw w.parseError(BAD_INPUT)
    }
    Some(URL(scheme, authority, path, query, fragment))
  }


  private def readScheme(w: CodeWalker): UString = {
    w.readAll(_ != COLON_CODE)
    val scheme = w.getCurrentSelection
    if(!w.tryRead(COLON_SLASHES)) {
      throw w.parseError(MISSING_TOKEN,
        SHOULD -> COLON_SLASHES)
    }
    w.commit()
    scheme
  }


  def readUser(w: CodeWalker): Option[UString] = {
    val pw = w.patternWalker(64)
    pw.readAll(UChar.isAlphanumeric)
      .test(AT_CODE)
      .get
      .filter(!_.isEmpty)
  }


  def readAuthority(w: CodeWalker): URL.Authority = {
    val user: Option[UString] = readUser(w)

    val host: WQName = WQName.parser(isValidDomainChar)
      .tryRead(w)
      .getOrElse(throw w.parseError(BAD_INPUT))

    val port = if(w.tryRead(COLON_CODE)) {
      NumericToken.reader.tryRead(w) match {
        case Some(i: IntegralToken) => Some(i.intValue)
        case _ => throw w.parseError(WRONG_TOKEN,
          SHOULD -> U"INTEGER")
      }
    } else {
      None
    }

    new URL.Authority(host, user, port)
  }


  def isValidQueryChar(ch: Int): Boolean = UChar.isAlphanumeric(ch) ||
    VALID_QUERY_CHAR.contains(ch.toChar)


  def decode(w: CodeWalker): UString = {
    val buffer = new ByteArrayOutputStream()
    var hasMore = true
    while(hasMore) {
      val ch = w.tryRead(isValidQueryChar(_))
      if(ch == -1) {
        if(w.tryRead(PERCENT)) {
          val d1 = w.tryRead(IntegralToken.isValidHexDigit(_))
          val d2 = w.tryRead(IntegralToken.isValidHexDigit(_))
          if(d1 == -1 || d2 == -1) {
            w.parseError(MISSING_TOKEN, SHOULD -> "TWO_HEX_DIGITS")
          }
          buffer.write(IntegralToken.hexByte(d1, d2))
        } else {
          hasMore = false
        }
      } else {
        buffer.write(UChar.encodeUtf8(ch))
      }
    }
    w.commit()
    UString.of(buffer.toByteArray)
  }


  def readQueryParam(w: CodeWalker): URL.QueryParam = {
    val key = IdentifierToken.reader
      .tryRead(w)
      .getOrElse(throw w.parseError(
        MISSING_TOKEN, SHOULD -> "QUERY_PARAM_NAME"))
      .value

    w.commit()

    if(w.tryRead(EQ)) {
      w.commit()
      new URL.QueryParam(key, decode(w))
    } else {
      new URL.QueryParam(key, "")
    }
  }


  def readQuery(w: CodeWalker): URL.Query =
    if(!w.tryRead(QUESTION_MARK)) {
      URL.NO_QUERY
    } else {
      w.commit()
      var params: Seq[URL.QueryParam] = Nil
      var hasMore = true
      while(hasMore) {
        params = params :+ readQueryParam(w)
        hasMore = w.tryRead(AMP)
        w.commit()
      }
      new URL.Query(params)
    }


  def readFragment(w: CodeWalker): Option[UString] =
    if(w.tryRead(HASH)) {
      w.commit()
      val parsed = IdentifierToken.reader
        .tryRead(w)
        .map(_.value)
      if(parsed.isEmpty) {
        throw w.parseError(MISSING_TOKEN, SHOULD -> "FRAGMENT_NAME")
      }
      parsed
    } else {
      None
    }
}
