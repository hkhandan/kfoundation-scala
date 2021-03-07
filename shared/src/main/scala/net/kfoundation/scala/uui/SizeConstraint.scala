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


object SizeConstraint {
  val NONE = new SizeConstraint(LengthConstraint.NONE, LengthConstraint.NONE)
}


class SizeConstraint(val width: LengthConstraint, val height: LengthConstraint)
{
  def canEqual(other: Any): Boolean = other.isInstanceOf[SizeConstraint]

  override def equals(other: Any): Boolean = other match {
    case that: SizeConstraint =>
      (that canEqual this) &&
        width == that.width &&
        height == that.height
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(width, height)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}