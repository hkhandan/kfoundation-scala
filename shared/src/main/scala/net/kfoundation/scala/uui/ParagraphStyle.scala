// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui

import net.kfoundation.scala.UString


object ParagraphStyle {
  val NORMAL: UString = "NORMAL"
  val BOLD_KEY: UString = "b"
  val ITALIC_KEY: UString = "i"
  val UNDERLINED: UString = "u"
  val DEFAULT = new ParagraphStyle(TextStyle.DEFAULT)
}


/** Paragraph style used to render LoTeX. */
class ParagraphStyle(
  val base: TextStyle,
  customStyles: Map[UString, TextStyle] = Map.empty)
{
  import ParagraphStyle._

  val styleMap: Map[UString, TextStyle] = Map(
    BOLD_KEY -> base.bold,
    ITALIC_KEY -> base.italic,
    UNDERLINED -> base.withUnderline) ++
    customStyles
}