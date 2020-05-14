package net.kfoundation.lang.lex

import net.kfoundation.lang.{CodeLocation, CodeRange}

class LexicalError(file: String, location: CodeLocation, message: String, cause: Throwable)
  extends Exception(s"$file@${location.getRow}:${location.getCol}: $message", cause)
{
  def this(file: String, location: CodeLocation, message: String) =
    this(file, location, message, null)

  def this(range: CodeRange, message: String) =
    this(range.file, range.begin, message)
}