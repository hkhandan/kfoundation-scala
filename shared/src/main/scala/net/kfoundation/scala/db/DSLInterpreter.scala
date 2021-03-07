// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.db

import net.kfoundation.scala.UString
import net.kfoundation.scala.db.DSL._

trait DSLInterpreter {
  def append(builder: UString.Builder, predicate: Predicate): Unit
  def append(builder: UString.Builder, delete: Delete): Unit
  def append(builder: UString.Builder, select: Select): Unit
  def append(builder: UString.Builder, aggregate: Aggregate): Unit
}