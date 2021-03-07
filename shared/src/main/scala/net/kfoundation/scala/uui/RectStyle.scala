// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui


object RectStyle {
  val ALL_ZERO_LENGTH: Cardinal[Length] = Cardinal(Length.ZERO)
  val ALL_ZERO_INT: Cardinal[Int] = Cardinal(0)

  val DEFAULT: RectStyle = apply()

  def apply(
    size: SizeConstraint = SizeConstraint.NONE,
    margin: Cardinal[Length] = ALL_ZERO_LENGTH,
    padding: Cardinal[Length] = ALL_ZERO_LENGTH,
    border: OptionCardinal[BorderStyle] = BorderStyle.NONE,
    cornersRadii: Cardinal[Int] = ALL_ZERO_INT,
    shadow: Option[Shadow] = None,
    blur: Double = 0,
    fill: Fill = Fill.color(Color.CLEAR),
    cursor: Cursor = Cursor.ARROW) =
    new RectStyle(size, margin, padding, border, cornersRadii, shadow, blur,
      fill, cursor)
}


class RectStyle(
  val size: SizeConstraint,
  val margin: Cardinal[Length],
  val padding: Cardinal[Length],
  val border: OptionCardinal[BorderStyle],
  val cornerRadii: Cardinal[Int],
  val shadow: Option[Shadow],
  val blur: Double,
  val fill: Fill,
  val cursor: Cursor)
{
  def withMargin(m: Cardinal[Length]) =
    new RectStyle(size, m, padding, border, cornerRadii, shadow, blur, fill, cursor)

  def withPadding(p: Cardinal[Length]) =
    new RectStyle(size, margin, p, border, cornerRadii, shadow, blur, fill, cursor)

  def withBorders(b: OptionCardinal[BorderStyle]) =
    new RectStyle(size, margin, padding, b, cornerRadii, shadow, blur, fill, cursor)

  def withCornerRadius(r: Cardinal[Int]) =
    new RectStyle(size, margin, padding, border, r, shadow, blur, fill, cursor)

  def withShadow(s: Shadow) =
    new RectStyle(size, margin, padding, border, cornerRadii, Some(s), blur, fill, cursor)

  def withoutShadow =
    new RectStyle(size, margin, padding, border, cornerRadii, None, blur, fill, cursor)

  def withColor(c: Color) =
    new RectStyle(size, margin, padding, border, cornerRadii, shadow, blur,
      Fill.color(c), cursor)

  def withSize(d: SizeConstraint) =
    new RectStyle(d, margin, padding, border, cornerRadii, shadow, blur, fill, cursor)

  def withCursor(c: Cursor) =
    new RectStyle(size, margin, padding, border, cornerRadii, shadow, blur, fill, c)

  def withBlue(b: Double) =
    new RectStyle(size, margin, padding, border, cornerRadii, shadow, b, fill, cursor)
}