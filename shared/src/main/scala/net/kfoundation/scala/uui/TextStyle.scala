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


object TextStyle {
  sealed class FontWeight
  object FontWeight {
    val BOLD = new FontWeight
    val NORMAL = new FontWeight
  }

  sealed class FontStyle
  object FontStyle {
    val ITALIC = new FontStyle
    val NORMAL = new FontStyle
  }

  sealed class TextDecoration
  object TextDecoration {
    val UNDERLINE = new TextDecoration
    val NONE = new TextDecoration
  }

  val DEFAULT = new TextStyle(None, None, Color.BLACK, FontWeight.NORMAL,
    TextDecoration.NONE, FontStyle.NORMAL)

  def apply(family: UString, size: Int,
    color: Color = Color.BLACK,
    weight: FontWeight = FontWeight.NORMAL,
    style: FontStyle = FontStyle.NORMAL,
    decoration: TextDecoration = TextDecoration.NONE) =
    new TextStyle(Some(family), Some(size), color, weight, decoration, style)
}


class TextStyle(val family: Option[UString], val size: Option[Int],
  val color: Color, val weight: TextStyle.FontWeight,
  val decoration: TextStyle.TextDecoration, val style: TextStyle.FontStyle)
{
  import TextStyle._

  def withSize(s: Int) = new TextStyle(family, Some(s), color, weight, decoration, style)
  def withWeight(w: FontWeight) = new TextStyle(family, size, color, w, decoration, style)
  def bold: TextStyle = withWeight(FontWeight.BOLD)
  def notBold: TextStyle = withWeight(FontWeight.NORMAL)
  def withStyle(s: FontStyle) = new TextStyle(family, size, color, weight, decoration, s)
  def italic: TextStyle = withStyle(FontStyle.ITALIC)
  def notItalic: TextStyle = withStyle(FontStyle.NORMAL)
  def withDecoration(d: TextDecoration) = new TextStyle(family, size, color, weight, d, style)
  def withUnderline: TextStyle = withDecoration(TextDecoration.UNDERLINE)
  def withoutDecoration: TextStyle = withDecoration(TextDecoration.NONE)
  def withColor(c: Color) = new TextStyle(family, size, c, weight, decoration, style)
}