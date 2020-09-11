package net.kfoundation.scala.serialization.internals

import net.kfoundation.scala.{UChar, UString}

object CommonSymbols {
  val OPEN_BRACE: UChar = '['
  val CLOSE_BRACE: UChar = ']'
  val OPEN_CURLY_BRACE: UChar = '{'
  val CLOSE_CURLY_BRACE: UChar = '}'
  val EQUAL: UChar = '='
  val TRUE: UString = "true"
  val FALSE: UString = "false"
  val NULL: UString = "null"
  val DOUBLE_QUOTE: UChar = '"'
  val SPACE: Char = ' '
  val COLON: UChar = ':'
  val NEWLINE: Char = '\n'
  val DASH: Char = '-'
  val COMMA: UChar = ','

  def booleanToString(value: Boolean): UString =
    if(value) {
      TRUE
    } else {
      FALSE
    }
}