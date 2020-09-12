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

  def getItems: List[Any] = List.from(items)

  override def toString: String = items.toString()
}
