// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse.syntax

import net.kfoundation.scala.UString
import net.kfoundation.scala.parse.lex.Token



object SyntaxError {

  private def describe(t: Token[_]): String = {
    val kind = t.getClass.getSimpleName.replace("Token", "").toLowerCase
    kind + " '" + t.value + "'"
  }


  def expected(file: String, lastToken: Option[Token[_]],
    thisToken: Option[Token[_]], expectation: UString): SyntaxError =
  {
    val l: String = file + thisToken.map(t => t.begin.getLocationTag)
      .orElse(lastToken.map(t => t.end.getLocationTag))
      .getOrElse("")

    val found: String = thisToken.map("'" + _.value.toString + "'")
      .getOrElse("nothing")

    val after: String = lastToken.map(t => " after " + describe(t))
      .getOrElse("")

    new SyntaxError(s"$l: $expectation expected$after but $found found")
  }

}



class SyntaxError(message: String, cause: Throwable)
  extends Exception(message, cause)
{
  def this(message: String) = this(message, null)
}
