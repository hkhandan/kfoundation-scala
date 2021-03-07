// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui


/** A list of length constraints. Used to layout rows and columns. */
class LengthConstraintList(val items: Seq[LengthConstraint]) {
  def +(item: LengthConstraint): LengthConstraintList =
    new LengthConstraintList(items.appended(item))
}
