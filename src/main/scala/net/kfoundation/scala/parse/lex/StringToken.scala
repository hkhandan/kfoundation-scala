package net.kfoundation.scala.parse.lex

import java.io.ByteArrayOutputStream

import net.kfoundation.scala.parse.CodeRange
import net.kfoundation.scala.{UChar, UString}

import scala.annotation.tailrec

object StringToken {
  private val DOUBLE_QUOTE: UChar = '"'
  private val BACKSLASH: UChar = '\\'

  object reader extends TokenReader[StringToken] {
    def tryRead(w: CodeWalker): Option[StringToken] =
      if(w.tryRead(DOUBLE_QUOTE)) {
        val str = readStringBody(w, new ByteArrayOutputStream(), escaped = false)
        val range = w.commit()
        Some(new StringToken(range, str))
      } else {
        w.rollback()
        None
      }
  }

  @tailrec
  private def readStringBody(w: CodeWalker, b: ByteArrayOutputStream,
    escaped: Boolean): UString = w.tryReadUChar match
  {
    case Some(ch) =>
      if(escaped) {
        b.write(unescape(w, ch))
        readStringBody(w, b, escaped = false)
      } else if(ch.equals(BACKSLASH)) {
        readStringBody(w, b, escaped = true)
      } else if(ch.equals(DOUBLE_QUOTE)) {
        UString.of(b.toByteArray.toSeq)
      } else {
        ch.printToStream(b)
        readStringBody(w, b, escaped = false)
      }

    case None => throw w.lexicalErrorAtCurrentLocation(
      "End of stream reached but end of string not found. Start at " +
        w.getBegin.getLocationTag)
  }

  private def unescape(w: CodeWalker, ch: UChar): Int = ch.codePoint match {
    case 'n' => '\n'
    case 't' => '\t'
    case 'b' => '\b'
    case 'r' => '\r'
    case 'u' => throw w.lexicalErrorAtCurrentLocation(
      "Escape sequence \\u is not a language feature in current version")
    case '"' => '"'
    case '\\' => '\\'
    case _ => throw w.lexicalErrorAtCurrentLocation(
      "Invalid escape sequence '\\" + ch + "'")
  }
}

class StringToken(range: CodeRange, value: UString)
  extends Token[UString](range, value)