// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui

object Fill {
  sealed class Repetition
  val REPEAT_X = new Repetition
  val REPEAT_Y = new Repetition
  val REPEAT_XY = new Repetition
  val NO_REPEAT = new Repetition


  class ColorFill(val color: Color) extends Fill

  class LinearGradient(val color1: Color, val color2: Color, val angle: Angle) extends Fill

  class Image(val url: String, val position: Position, val scale: Scale,
    val repeat: Repetition) extends Fill

  def color(color: Color): ColorFill = new ColorFill(color)

  def gradient(color1: Color, color2: Color, rotate: Angle = Angle.ZERO) =
    new LinearGradient(color1, color2, rotate)

  def image(url: String, position: Position = Position.ZERO,
      scale: Scale = Scale.NONE, repeat: Repetition = NO_REPEAT): Image =
    new Image(url, position, scale, repeat)
}


trait Fill
