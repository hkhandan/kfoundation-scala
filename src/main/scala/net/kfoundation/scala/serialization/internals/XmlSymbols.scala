package net.kfoundation.scala.serialization.internals

import net.kfoundation.scala.UChar
import net.kfoundation.scala.UString.UStringInterpolation

object XmlSymbols {
  val VERSION = U"version"
  val ENCODING = U"encoding"
  val PROLOG_BEGIN = U"<?xml"
  val PROLOG_END = U"?>"
  val COMMENT_BEGIN = U"<!--"
  val COMMENT_END = U"-->"
  val LT_SLASH = U"</"
  val SLASH_GT = U"/>"
  val LT: UChar = '<'
  val TAG_BEGIN_CP: Int = LT.codePoint
  val GT: UChar = '>'
  val EQ: UChar = '='
  val AMP: UChar = '&'
  val AMP_CP: Int = AMP.codePoint
  val SEMICOLON: UChar = ';'
}