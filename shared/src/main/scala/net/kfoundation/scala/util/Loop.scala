// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.util


object Loop {

  def apply(b: Int, fn: Int => Unit): Unit = apply(0, b, fn)

  def apply(a: Int, b: Int, fn: Int => Unit): Unit = {
    val step = if(a > b) -1 else 1
    var i = a
    while(i < b) {
      fn(i)
      i += step
    }
  }

}
