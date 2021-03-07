// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.util

import net.kfoundation.scala.UString
import net.kfoundation.scala.UString.Interpolator
import net.kfoundation.scala.parse.lex.CodeWalker
import net.kfoundation.scala.parse.{ParseError, Parser}


object WQName {
  private val DOT: Int = '.'
  private val PARSER: Parser[WQName] = parser(_ != DOT)


  def parser(isValid: Int => Boolean): Parser[WQName] = (w: CodeWalker) => {
    var parts: Seq[UString] = Nil
    var hasMore = true
    while (hasMore) {
      hasMore = w.readAll(isValid) > 0
      if (hasMore) {
        parts = parts :+ w.getCurrentSelection
        hasMore = w.tryRead(DOT)
        w.commit()
      }
    }
    if (parts.isEmpty) {
      None
    } else {
      w.commit()
      Some(new WQName(parts))
    }
  }


  /**
   * @throws ParseError net.kfoundation.INVALID_DOMAIN_NAME(was)
   * @param str String to be parsed
   * @return Parsed DomainName
   */
  def apply(str: UString): WQName = {
    val walker = CodeWalker.of(str)
    PARSER.tryRead(walker)
      .filter(_ => !walker.hasMore)
      .getOrElse(throw new ParseError(
        "net.kfoundation.INVALID_DOMAIN_NAME",
        U"was" -> str))
  }


  def apply(parts: Seq[UString]): WQName = new WQName(parts)
}


/**
 * Obejct model for representation and manipulation of well-qualified names.
 * A WQName is a set of identifiers connected together using '.' delimiter.
 */
class WQName(val parts: Seq[UString]) {
  def last: UString = parts.last

  def parent: WQName = new WQName(parts.dropRight(1))

  def isEmpty: Boolean = parts.isEmpty

  def toUString: UString = UString.join(parts, '.')

  override def toString: String = toUString.toString()

  override def equals(other: Any): Boolean = other match {
    case that: WQName => parts == that.parts
    case _ => false
  }
}
