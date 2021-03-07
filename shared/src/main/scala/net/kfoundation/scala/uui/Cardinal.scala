// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui

import net.kfoundation.scala.util.Zero

import scala.language.implicitConversions


object Cardinal {
  def apply[T](all: T): Cardinal[T] = new Cardinal[T](all, all, all, all)

  def of[T](c: OptionCardinal[T])(implicit zero: Zero[T]): Cardinal[T] = {
    val z = zero.value
    new Cardinal[T](c.top.getOrElse(z), c.right.getOrElse(z), c.bottom.getOrElse(z),
      c.left.getOrElse(z))
  }

  def apply[T](top: T, right: T, bottom: T, left: T) =
    new Cardinal[T](top, right, bottom, left)

  def top[T](value: T, fill: T) = new Cardinal[T](value, fill, fill, fill)
  def right[T](value: T, fill: T) = new Cardinal[T](fill, value, fill, fill)
  def bottom[T](value: T, fill: T) = new Cardinal[T](fill, fill, value, fill)
  def left[T](value: T, fill: T) = new Cardinal[T](fill, fill, fill, value)

  def zero[T](implicit zero: Zero[T]): Cardinal[T] = apply(zero.value)
}



/**
 * A Cardinal is a collection of 4 values that describe a cardinally qualified
 * quadrilateral value.
 */
class Cardinal[T](val top: T, val right: T, val bottom: T, val left: T) {
  /** replaced this `top` field with the one given. */
  def withTop(value: T) = new Cardinal[T](value, right, bottom, left)


  /** replaced this `right` field with the one given. */
  def withRight(value: T) = new Cardinal[T](top, value, bottom, left)


  /** replaced this `bottom` field with the one given. */
  def withBottom(value: T) = new Cardinal[T](top, right, value, left)


  /** replaced this `left` field with the one given. */
  def withLeft(value: T) = new Cardinal[T](top, right, bottom, value)


  /**
   * Replaces fields of this object with the ones that are available in
   * the input parameter. */
  def |(c: OptionCardinal[T]) = new Cardinal[T](c.top.getOrElse(top),
    c.right.getOrElse(right), c.bottom.getOrElse(bottom),
    c.left.getOrElse(left))


  /**
   * Returns the values of this object as a sequence in the following order:
   * top, right, bottom, left.
   */
  def toSeq: Seq[T] = Seq(top, right, bottom, left)


  /**
   * Tests if all the fields of this object are equal.
   */
  def isUniform: Boolean = top == right && right == bottom && bottom == left


  /**
   * If all the values of this object are equal returns that value, otherwise
   * returns None.
   */
  def asExact: Option[T] = if(isUniform) Some(top) else None
}