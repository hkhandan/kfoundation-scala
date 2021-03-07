// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui


object Position {
  val ZERO: Position = of(Length.ZERO, Length.ZERO)

  def of(left: Length, top: Length) = new Position(left, top)
}


class Position(val left: Length, val top: Length) {
  def withLeft(l: Length) = new Position(l, top)
  def withTop(t: Length) = new Position(left, t)

  def canEqual(other: Any): Boolean = other.isInstanceOf[Position]

  override def equals(other: Any): Boolean = other match {
    case that: Position =>
      (that canEqual this) &&
        left == that.left &&
        top == that.top
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(left, top)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}