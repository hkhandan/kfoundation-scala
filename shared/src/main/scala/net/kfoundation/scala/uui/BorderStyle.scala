// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui

import scala.language.implicitConversions


object BorderStyle {
  val DEFAULT_SIZE: Length = Length.ofPixels(1)
  val DEFAULT_COLOR: Color = Color.BLACK
  val DEFAULT_PATTEN: LinePattern = LinePattern.SOLID

  val NONE: OptionCardinal[BorderStyle] = OptionCardinal.none

  def of(
    size: Length = DEFAULT_SIZE,
    style: LinePattern = DEFAULT_PATTEN,
    color: Color = DEFAULT_COLOR) =
    new BorderStyle(size, style, color)
}


class BorderStyle(val size: Length, val pattern: LinePattern,
  val color: Color)
{
  def withSize(s: Length) = new BorderStyle(s, pattern, color)
  def withStyle(s: LinePattern) = new BorderStyle(size, s, color)
  def withColor(c: Color) = new BorderStyle(size, pattern, c)
}