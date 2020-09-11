package net.kfoundation.scala.util


object Loop {

  def apply(b: Int, fn: Int => Unit): Unit = apply(0, b, fn)

  def apply(a: Int, b: Int, fn: Int => Unit): Unit = {
    var i = a
    while(i < b) {
      fn(i)
      i += 1
    }
  }

}
