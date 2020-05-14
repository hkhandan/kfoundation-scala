package net.kfoundation.lang.syntax

import net.kfoundation.UString
import net.kfoundation.lang.lex.Token


object SyntaxError {
  private def describe(t: Token[_]): String = {
    val kind = t.getClass.getSimpleName.replace("Token", "").toLowerCase
    kind + " '" + t.value + "'"
  }

  def expected(file: String, lastToken: Option[Token[_]],
    thisToken: Option[Token[_]], expectation: UString): SyntaxError =
  {
    val l: String = file + thisToken.map(t => t.begin.getShortDescription)
      .orElse(lastToken.map(t => t.end.getShortDescription))
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
