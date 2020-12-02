// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.util

class SimpleStack[T] {
  private var items = List[T]()

  def push(item: T): Unit = items = items :+ item

  def pop(): Option[T] = {
    val item = items.lastOption
    if(item.isDefined) {
      items = items.dropRight(1)
    }
    item
  }

  def peek(): Option[T] = items.lastOption

  def getItems: List[T] = items

  override def toString: String = items.toString()
}
