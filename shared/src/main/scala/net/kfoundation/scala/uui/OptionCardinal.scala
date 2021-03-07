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



object OptionCardinal {
  /** Creates an all-None OptionCardinal. */
  def none[T] = new OptionCardinal[T](None, None, None, None)


  /** Creates an OptionCardinal with al its values being the given parameter. */
  def apply[T](all: T) =
    new OptionCardinal[T](Some(all), Some(all), Some(all), Some(all))


  /**
   * Creates an OptionCardinal with the given parameters.
   */
  def apply[T](top: T, right: T, bottom: T, left: T) =
    new OptionCardinal[T](Some(top), Some(right), Some(bottom), Some(left))


  /** Creates an OptionCardinal with only its `top` field set. */
  def top[T](top: T) = new OptionCardinal[T](Some(top), None, None, None)


  /** Creates an OptionCardinal with only its `right` field set. */
  def right[T](right: T) = new OptionCardinal[T](None, Some(right), None, None)


  /** Creates an OptionCardinal with only its `bottom` field set. */
  def bottom[T](bottom: T) = new OptionCardinal[T](None, None, Some(bottom), None)


  /** Creates an OptionCardinal with only its `left` field set. */
  def left[T](left: T) = new OptionCardinal[T](None, None, None, Some(left))
}



/**
 * An OptionCardinal value is a collection of 4 Option values that can be
 * identified cardinally.
 */
class OptionCardinal[T](val top: Option[T], val right: Option[T],
  val bottom: Option[T], val left: Option[T])
{
  /** Replaces the `top` field of this object with the given value.*/
  def withTop(value: T) = new OptionCardinal[T](Some(value), right, bottom, left)


  /** Replaces the `right` field of this object with the given value.*/
  def withRight(value: T) = new OptionCardinal[T](top, Some(value), bottom, left)


  /** Replaces the `bottom` field of this object with the given value.*/
  def withBottom(value: T) = new OptionCardinal[T](top, right, Some(value), left)


  /** Replaces the `left` field of this object with the given value.*/
  def withLeft(value: T) = new OptionCardinal[T](top, right, bottom, Some(value))


  /** Replaces the `top` field of this object with None. */
  def withoutTop = new OptionCardinal[T](None, right, bottom, left)


  /** Replaces the `right` field of this object with None. */
  def withoutRight = new OptionCardinal[T](top, None, bottom, left)


  /** Replaces the `bottom` field of this object with None. */
  def withoutBottom = new OptionCardinal[T](top, right, None, left)


  /** Replaces the `left` field of this object with None. */
  def withoutLeft = new OptionCardinal[T](top, right, bottom, None)


  /**
   * Replaces the fields of this object with the corresponding ones that are not
   * None in the input parameter.
   */
  def |(other: OptionCardinal[T]) = new OptionCardinal[T](
    other.top.orElse(top),
    other.right.orElse(right),
    other.bottom.orElse(bottom),
    other.left.orElse(left))


  /**
   * True if all fields of this object are the same.
   */
  def isUniform: Boolean = top.equals(right) && right.equals(bottom) &&
    bottom.equals(left)


  /**
   * True if all fields of this object are None.
   */
  def isEmpty: Boolean = top.isEmpty && right.isEmpty && bottom.isEmpty &&
    left.isEmpty


  def canEqual(other: Any): Boolean = other.isInstanceOf[OptionCardinal[T]]


  override def equals(other: Any): Boolean = other match {
    case that: OptionCardinal[T] =>
      (that canEqual this) &&
        top == that.top &&
        right == that.right &&
        bottom == that.bottom &&
        left == that.left
    case _ => false
  }


  override def hashCode(): Int = {
    val state = Seq(top, right, bottom, left)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
