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


object Length {
  sealed class LengthUnit()
  object LengthUnit {
    val POINTS = new LengthUnit
    val PIXELS = new LengthUnit
    val PERCENT = new LengthUnit
    val CENTIMETERS = new LengthUnit
    val MILLIMETERS = new LengthUnit
    val INCHES = new LengthUnit
  }

  /** A scalar that can be given a unit to become a Length */
  class Scalar(value: Double) {
    import Length.LengthUnit._
    def px = new Length(value, PIXELS)
    def pt = new Length(value, POINTS)
    def percent = new Length(value, PERCENT)
    def %% : Length = percent
    def cm = new Length(value, CENTIMETERS)
    def mm = new Length(value, MILLIMETERS)
    def in = new Length(value, INCHES)
    def *(other: Length) = new Length(value * other.value, other.unit)
  }


  val ZERO = new Length(0, LengthUnit.PIXELS)
  val FULL = new Length(100, LengthUnit.PERCENT)
  val CARDINAL_ZERO: Cardinal[Length] = Cardinal.apply(Length.ZERO)

  def of(value: Double): Scalar = new Scalar(value)
  def of(value: Long): Scalar = new Scalar(value)

  def of(value: Double, unit: LengthUnit) = new Length(value, unit)
  def ofPixels(value: Double) = new Length(value, LengthUnit.PIXELS)
  def ofPoints(value: Double) = new Length(value, LengthUnit.POINTS)
  def ofPercent(value: Double) = new Length(value, LengthUnit.PERCENT)
  def ofCentimeters(value: Double) = new Length(value, LengthUnit.CENTIMETERS)
  def ofMillimeters(value: Double) = new Length(value, LengthUnit.MILLIMETERS)
  def ofInches(value: Double) = new Length(value, LengthUnit.INCHES)

  def postfix(unit: LengthUnit): String = unit match {
    case LengthUnit.CENTIMETERS => "cm"
    case LengthUnit.INCHES => "\""
    case LengthUnit.MILLIMETERS => "mm"
    case LengthUnit.PERCENT => "%"
    case LengthUnit.PIXELS => "px"
    case LengthUnit.POINTS => "pt"
    case _ => "???"
  }
}


/** Describes a one-dimensional length with its associated unit. */
class Length(val value: Double, val unit: Length.LengthUnit) {

  /** Multiplies this length value by the given scalar. */
  def *(scalar: Double): Length = new Length(value * scalar, unit)

  def canEqual(other: Any): Boolean = other.isInstanceOf[Length]

  override def equals(other: Any): Boolean = other match {
    case that: Length =>
      (that canEqual this) &&
        value == that.value &&
        unit == that.unit
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(value, unit)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = f"$value%.2f${Length.postfix(unit)}"
}