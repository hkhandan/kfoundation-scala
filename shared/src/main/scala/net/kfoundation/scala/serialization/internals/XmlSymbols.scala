// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization.internals

import net.kfoundation.scala.UChar
import net.kfoundation.scala.UString.Interpolator



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
  val GT_CP: Int = GT.codePoint
  val EQ: UChar = '='
  val AMP: UChar = '&'
  val AMP_CP: Int = AMP.codePoint
  val SEMICOLON: UChar = ';'
}