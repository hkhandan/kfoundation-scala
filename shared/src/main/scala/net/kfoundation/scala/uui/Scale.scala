// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui


object Scale {
  val NONE = new Scale(None, None)

  def apply(width: Option[Length], height: Option[Length]) =
    new Scale(width, height)

  def apply(width: Length, height: Length) =
    new Scale(Some(width), Some(height))

  def width(w: Length) = new Scale(Some(w), None)

  def height(h: Length) = new Scale(Some(h), None)
}


class Scale(val width: Option[Length], val height: Option[Length])
