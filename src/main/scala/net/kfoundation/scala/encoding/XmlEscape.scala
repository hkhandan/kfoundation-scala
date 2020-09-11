package net.kfoundation.scala.encoding

import net.kfoundation.scala.UString.UStringInterpolation
import net.kfoundation.scala.{UChar, UString}

object XmlEscape {
  import UChar.of

  private val UNESCAPE_MAP: Map[UString, UChar] = Map(
    U"quot" -> of('"'),
    U"amp" -> of('&'),
    U"apos" -> of('\''),
    U"lt" -> of('<'),
    U"gt" -> of('>'))

  private val ESCAPE_MAP: Map[UChar, UString] =
    UNESCAPE_MAP.map(kv => (kv._2, kv._1))

  def escapeOne(char: UChar): UString = ESCAPE_MAP.get(char)
    .getOrElse(UString.of(char))

  def unescapeOne(str: UString): UString = UNESCAPE_MAP.get(str)
    .map(UString.of)
    .getOrElse(str)
}
