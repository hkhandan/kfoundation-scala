// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization.internals

import net.kfoundation.scala.UString
import net.kfoundation.scala.serialization.ObjectStreamError
import net.kfoundation.scala.util.SimpleStack



object ObjectStreamStateMachine {
  private class StackItem(val isCollection: Boolean, val name: Option[UString]) {
    override def toString: String = name.map(_.toString).
      getOrElse("<no-name>") + (if(isCollection) "[]" else "")
  }

  object State extends Enumeration {
    val STREAM_BEGIN, STREAM_END, OBJECT_BEGIN, OBJECT_END, COLLECTION_BEGIN,
    COLLECTION_END, PROPERTY, LITERAL = Value
  }
}



class ObjectStreamStateMachine {
  import ObjectStreamStateMachine._
  import State._


  private val stack = new SimpleStack[StackItem]
  private var state = State.STREAM_BEGIN
  private var _isFirst = true


  private def validateTransition(s: State.Value): Unit = {
    val isAllowed: Boolean = state match {
      case STREAM_BEGIN => s == OBJECT_BEGIN
      case STREAM_END => false
      case OBJECT_BEGIN => s == PROPERTY || s == OBJECT_END
      case OBJECT_END => s == PROPERTY || s == OBJECT_END || s == COLLECTION_END || (s == OBJECT_BEGIN && isInCollection)
      case COLLECTION_BEGIN => s == OBJECT_BEGIN || s == COLLECTION_END
      case COLLECTION_END => s == PROPERTY || s == OBJECT_END
      case PROPERTY => s == LITERAL || s == OBJECT_BEGIN || s == COLLECTION_BEGIN
      case LITERAL => s == PROPERTY || s == OBJECT_END
      case _ => false
    }

    if(!isAllowed) {
      val negative = if(isInCollection) "" else "not "
      throw error(s"Illegal attempt to transition from $state to $s (${negative}in collection)")
    }

    state = s
  }


  def getState: State.Value = state


  private def objectBegin(name: Option[UString]): Unit = {
    validateTransition(OBJECT_BEGIN)
    stack.push(new StackItem(false, name))
    _isFirst = true
  }


  def objectBegin(name: UString): Unit = objectBegin(Some(name))


  def objectBegin(): Unit = objectBegin(None)


  def objectEnd(): Option[UString] =
    stack.pop()
      .map(item => {
        validateTransition(OBJECT_END)
        _isFirst = false
        item.name
      })
      .getOrElse(throw error(
        "Illegal attempt to read object end with no corresponding beginning"))


  def objectEnd(name: UString): Unit = objectEnd().foreach(n =>
    if(!n.equals(name)) {
      throw new ObjectStreamError(s"Expected end of object $name, but found: $n")
    })


  private def collectionBegin(name: Option[UString]): Unit = {
    validateTransition(COLLECTION_BEGIN)
    _isFirst = true
    stack.push(new StackItem(true, name))
  }

  def collectionBegin(): Unit = collectionBegin(None)


  def collectionBegin(name: UString): Unit = collectionBegin(Some(name))


  def collectionEnd(): Option[UString] = stack.pop()
    .map(item => {
      validateTransition(COLLECTION_END)
      _isFirst = false
      item.name
    })
    .getOrElse(throw error(
      "Illegal attempt to read collection end with no corresponding beginning"))


  def property(): Unit = {
    validateTransition(PROPERTY)
    _isFirst = false
  }


  def literal(): Unit = validateTransition(LITERAL)


  def streamEnd(): Unit = validateTransition(STREAM_END)


  def isFirst: Boolean = _isFirst


  def isInCollection: Boolean = stack.peek().exists(_.isCollection)


  def peek: Option[UString] = stack.peek().flatMap(_.name)


  def error(message: String): ObjectStreamError = new ObjectStreamError(
    "Error at [" + getPath + "]: " + message)


  def getPath: String = stack.getItems.mkString(" > ")


  override def toString: String = "Path: " + getPath + ", State: " + state
}
