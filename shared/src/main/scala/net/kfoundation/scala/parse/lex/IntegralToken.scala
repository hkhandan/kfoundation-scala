// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse.lex

import net.kfoundation.scala.{UChar, UString}
import net.kfoundation.scala.parse.CodeRange
import net.kfoundation.scala.util.Loop

object IntegralToken {
  def hexToInt(ch: Int): Int =
    if(ch >= '0' && ch <= '9') ch - 48
    else if(ch >= 'A' && ch <= 'F') ch - 55
    else if(ch <= 'a' && ch <= 'f') ch - 87
    else -1

  def isValidHexDigit(ch: Int): Boolean = hexToInt(ch) != -1

  def hexByte(high: Int, low: Int): Int = hexToInt(high)*16 + hexToInt(low)

  def hexToInt(str: UString): Int = {
    val bytes = str.toUtf8
    var base = 1
    var dec = 0
    Loop(bytes.length - 1, 0, i => {
      val h = hexToInt(bytes(i))
      if(h == -1) {
        val ch = UChar.of(bytes(i).toChar)
        throw new NumberFormatException(s"Invalid hex character '$ch' in '$str'")
      }
      dec += h * base
      base = base * 16
    })
    dec
  }
}


/** A portion of input text that represents an integral number */
class IntegralToken(range: CodeRange, value: Long)
  extends NumericToken[Long](range, value)
{
  def intValue: Int = value.toInt
  def asDecimalToken: DecimalToken = new DecimalToken(range, value.toDouble)
}