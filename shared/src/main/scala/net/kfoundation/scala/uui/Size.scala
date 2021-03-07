// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui

import net.kfoundation.scala.{UObject, UString}

import scala.language.implicitConversions


object Size {
  val ZERO: Size = apply(Length.ZERO)
  val FIT: Size = apply(Length.FULL)

  def apply(width: Length, height: Length) = new Size(width, height)

  def apply(widthAndHeight: Length) = new Size(widthAndHeight, widthAndHeight)
}



class Size(val width: Length, val height: Length) extends UObject {
  def withWidth(w: Length) = new Size(w, height)

  def withHeight(h: Length) = new Size(width, h)

  override def appendTo(builder: UString.Builder): Unit = builder.appendAll(
    "Size(", width, ", ", height, ")")

  override def equals(other: Any): Boolean = other match {
    case that: Size =>
        width == that.width &&
        height == that.height
    case _ => false
  }
}