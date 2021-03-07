// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui


object Angle {
  sealed class AngleUnit
  val DEGREE = new AngleUnit
  val RADIAN = new AngleUnit
  val GRADIAN = new AngleUnit

  /**
   * A scalar value that can be given a unit to become an angular value.
   */
  class Scalar(val amount: Double) {
    def deg: Angle = new Angle(amount, DEGREE)
    def rad: Angle = new Angle(amount, RADIAN)
    def grad: Angle = new Angle(amount, GRADIAN)
  }

  val ZERO = new Angle(0, RADIAN)

  def of(amount: Double): Scalar = new Scalar(amount)

  def apply(amount: Double, unit: AngleUnit): Angle = new Angle(amount, unit)
}



/** Represents an angular value. */
class Angle(val amount: Double, val unit: Angle.AngleUnit) {
  import Angle._

  /** Converts the unit of this angle to degrees */
  def toDegrees: Angle = unit match {
    case DEGREE => this
    case RADIAN => new Angle(amount * Math.PI / 180, DEGREE)
    case GRADIAN => new Angle(amount * 10 / 9, DEGREE)
    case _ => throw new IllegalStateException()
  }


  /** Converts the unit of this angle to radians */
  def toRadians: Angle = unit match {
    case DEGREE => new Angle(amount * 180 / Math.PI, RADIAN)
    case RADIAN => this
    case GRADIAN => new Angle(amount * Math.PI / 200, RADIAN)
    case _ => throw new IllegalStateException()
  }


  /** Converts the unit of this angle to gradians */
  def toGradians: Angle = unit match {
    case DEGREE => new Angle(amount * 9/10, GRADIAN)
    case RADIAN => new Angle(amount * 200/Math.PI, GRADIAN)
    case GRADIAN => this
    case _ => throw new IllegalStateException()
  }
}