// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse.lex

import java.io.ByteArrayOutputStream

import net.kfoundation.scala.parse.CodeRange
import net.kfoundation.scala.{UChar, UString}

import scala.annotation.tailrec



object StringToken {
  private val DOUBLE_QUOTE: UChar = '"'
  private val BACKSLASH: UChar = '\\'


  /**
   * Attempt to read a C-escaped string from input. Supported escape sequences
   * are "\\" for one "\", "\n" for newline, "\r" for carriage return, "\t"
   * for tab, and "\b" for backspace. "\\u" for unicode codepoint is planned
   * to be supported in future.
   */
  object reader extends TokenReader[StringToken] {
    def tryRead(w: CodeWalker): Option[StringToken] =
      if(w.tryRead(DOUBLE_QUOTE)) {
        val str = readStringBody(w, new ByteArrayOutputStream(), escaped = false)
        val range = w.commit()
        Some(new StringToken(range, str))
      } else {
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
        ch.writeToStream(b)
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



/**
 * A portion of the input that is considered as literal text.
 */
class StringToken(range: CodeRange, value: UString)
  extends Token[UString](range, value)