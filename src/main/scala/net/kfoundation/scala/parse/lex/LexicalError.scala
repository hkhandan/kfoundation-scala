package net.kfoundation.scala.parse.lex

import net.kfoundation.scala.UString
import net.kfoundation.scala.parse.{CodeLocation, CodeRange}


class LexicalError(location: CodeLocation, message: UString, cause: Throwable)
  extends Exception(s"${location.getLocationTag} $message", cause)
{
  def this(location: CodeLocation, message: String) =
    this(location, message, null)

  def this(range: CodeRange, message: String) =
    this(range.begin, message)
}