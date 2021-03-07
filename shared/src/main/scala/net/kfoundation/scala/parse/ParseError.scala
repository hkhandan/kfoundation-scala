// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse

import net.kfoundation.scala.UString
import net.kfoundation.scala.UString.Interpolator
import net.kfoundation.scala.i18n.{LException, LMessage}
import net.kfoundation.scala.parse.ParseError.LEXICAL_ERROR
import net.kfoundation.scala.parse.lex.LexicalError


object ParseError {
  val MISSING_TOKEN: UString = "net.kfoundation.MISSING_TOKEN"
  val WRONG_TOKEN: UString = "net.kfoundation.MISSING_PART"
  val BAD_INPUT: UString = "net.kfoundation.BAD_INPUT"
  val LEXICAL_ERROR: UString = "net.kfoundation.LEXICAL_ERROR"
  val WAS: UString = "was"
  val SHOULD: UString = "should"
}


/**
 * A LException to report parse errors in a localizable way.
 */
class ParseError(message: LMessage, cause: Option[Throwable])
  extends LException(message, cause)
{
  def this(key: UString, params: (UString, UString)*) =
    this(LMessage(key, params:_*), None)

  def this(l: LexicalError) = this(
    LMessage(LEXICAL_ERROR,
      U"description" -> l.description,
      U"row" -> UString.of(l.location.getRow),
      U"col" -> UString.of(l.location.getCol)),
    Some(l))
}
