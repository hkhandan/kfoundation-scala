package net.kfoundation.scala.uui

import net.kfoundation.scala.util.Flow


object MouseEvents {
  class ModifierKeys(val shift: Boolean, val alt: Boolean, val control: Boolean,
    val meta: Boolean)

  object MouseButtons extends Enumeration {
    val MAIN, SECONDARY, AUXILIARY, OTHER = Value
  }

  type MouseButton = MouseButtons.Value

  class MouseData(val position: Position, val positionInFrame: Position,
    val positionOnScreen: Position, val button: MouseButton,
    val key: ModifierKeys)

  val NONE = new MouseEvents(Flow.closed, Flow.closed, Flow.closed, Flow.closed)
}


class MouseEvents(
  val onEnter: Flow.Inlet[MouseEvents.MouseData] = Flow.closed,
  val onLeave: Flow.Inlet[MouseEvents.MouseData] = Flow.closed,
  val moves: Flow.Inlet[MouseEvents.MouseData] = Flow.closed,
  val onClick: Flow.Inlet[MouseEvents.MouseData] = Flow.closed)
{
  import MouseEvents._

  private def select(first: Flow.Inlet[MouseData], second: Flow.Inlet[MouseData]):
    Flow.Inlet[MouseData] = first match {
      case _: Flow.Closed[MouseData] => second
      case _ => second match {
        case _: Flow.Closed[MouseData] => first
        case _ => second
      }
    }

  def &(other: MouseEvents): MouseEvents = new MouseEvents(
    select(onEnter, other.onEnter),
    select(onLeave, other.onLeave),
    select(moves, other.moves),
    select(onClick, onClick))
}