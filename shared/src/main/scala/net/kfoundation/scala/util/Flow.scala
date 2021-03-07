// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.util

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


object Flow {

  private trait Intake[T] {
    def accept(message: Try[T]): Unit
    def isActive: Boolean
    def trace: Trace
    def acceptInit(i: T)
  }


  private abstract class AbstractNode[I, O](init: Option[O])
    extends Flow[O] with Intake[I]
  {
    protected val serial: Int = serialCounter.incrementAndGet()
    private var outputs: Seq[Intake[O]] = Nil
    private var state: Option[O] = init
    private var forcedActiveStatus: Option[Boolean] = None

    protected def addOutput[T <: Intake[O]](n: T): T = {
      outputs = outputs :+ n
      n
    }

    def removeOutput(o: Intake[O]): Unit =
      outputs = outputs.filter(!_.equals(o))

    protected def forward(message: Try[O]): Unit = {
      outputs.foreach(_.accept(message))
      message.foreach(value => state = Some(value))
    }

    protected def forwardInit(o: O): Unit =
      if(state.isEmpty) {
        state = Some(o)
        outputs.foreach(n => n.acceptInit(o))
      }

    protected def trace(name: String): Trace = {
      val routes = outputs.map(_.trace)
        .map(_.paths)
        .fold(Seq[TraceNode]())((a, b) => a.appendedAll(b))
      if(routes.isEmpty) {
        new Trace(Seq(new TraceNode(serial, name, peek.map(_.toString), None)))
      } else {
        new Trace(routes.map(r => new TraceNode(serial, name,
          peek.map(_.toString), Some(r))))
      }
    }

    override def default(init: O): Flow[O] = addOutput(
      new ForwardNode[O](peek.orElse(Some(init)), "default").setInput(this))

    def map[T](fn: O => T): Flow[T] =
      addOutput(new MapNode[O, T](peek.map(fn), "map", fn))

    override def futureMap[S](fn: O => Future[S])(
        implicit ec: ExecutionContext): Flow[S] =
      addOutput(new FutureForwardNode(peek, fn, ec))

    override def optionMap[S](fn: O => Option[S]): Flow[S] =
      addOutput(new OptionForwardNode(peek, fn))

    def filter(predicate: O => Boolean): Flow[O] =
      addOutput(new FilterNode[O](peek.filter(predicate), predicate))

    def mix(other: Flow[O]): Flow[O] = {
      val node = new MixNode[O](other.peek.orElse(peek))
      addOutput(node.intake1)
      other match {
        case nb: AbstractNode[_, O] => nb.addOutput(node.intake2)
        case _ => throw new IllegalStateException("Non-standard DataNode")
      }
      node
    }

    override def remix(fn: Flow[O] => Flow[O]): Flow[O] = mix(fn(this))

    override def join[S](other: Flow[S]): Flow[(O, S)] = {
      val n = new JoinNode[O, S](this, other)
      addOutput(n.intake1)
      other match {
        case nb: AbstractNode[_, S] => nb.addOutput(n.intake2)
        case _ => throw new IllegalStateException("Non-standard DataNode")
      }
      n
    }

    override def foreach(consumer: O => Unit): Flow[O] = {
      addOutput(new ForeachNode[O](peek, consumer))
      this
    }

    override def forward(node: Inlet[O]): Flow[O] = {
      node match {
        case n: ForwardNode[O] =>
          addOutput(n.setInput(this))
          peek.foreach(s => forward(Success(s)))
        case _: ClosedInlet[O] => /* Nothing */
        case _ => throw new IllegalStateException(
          "User-defined InletNode subclass is not acceptable")
      }
      this
    }

    override def whenChanged: Flow[O] = whenChanged(x => x)

    override def whenChanged[S](fn: O => S): Flow[O] = addOutput(new DeltaFilterNode[O, S](peek, fn))

    override def delta: Flow[Delta[O]] =
      addOutput(new DeltaNode[O](peek))

    override def peek: Option[O] = state

    override def isActive: Boolean = forcedActiveStatus.getOrElse(
      outputs.exists(_.isActive))

    override def forceActiveStatus(status: Boolean): Flow[O] = {
      forcedActiveStatus = Some(status)
      this
    }

    override def handleErrors(fn: Throwable => Unit): Flow[O] =
      addOutput(new ForeachErrorNode(peek, fn))

    override def splitErrors: (Flow[O], Flow[Throwable]) = {
      val success = addOutput(new FilterSuccessNode(peek))
      val error = addOutput(new FilterErrorNode[O]())
      (success, error)
    }

    override def errors: Flow[Throwable] = addOutput(new ErrorsNode[O])

    override def recover(fn: PartialFunction[Throwable, O]): Flow[O] =
      addOutput(new RecoverNode[O](peek, fn))
  }


  private class FilterSuccessNode[T](init: Option[T])
    extends AbstractNode[T, T](init)
  {
    override def accept(message: Try[T]): Unit =
      if(message.isSuccess) {
        forward(message)
      }
    override def trace: Trace = trace("success-filter")
    override def acceptInit(i: T): Unit = forwardInit(i)
  }


  private class FilterErrorNode[T]() extends AbstractNode[T, Throwable](None) {
    override def accept(message: Try[T]): Unit = message match {
      case Failure(th) => forward(Success(th))
      case _ => /* Nothing */
    }
    override def trace: Trace = trace("error-filter")
    override def acceptInit(i: T): Unit = {}
  }


  private class MixNode[T](init: Option[T]) extends AbstractNode[T, T](init) {
    val intake1: Intake[T] = new Intake[T] {
      override def accept(message: Try[T]): Unit = forward(message)
      override def isActive: Boolean = MixNode.this.isActive
      override def trace: Trace = MixNode.this.trace("mix")
      override def acceptInit(i: T): Unit = forwardInit(i)
    }
    val intake2: Intake[T] = new Intake[T] {
      override def accept(message: Try[T]): Unit = forward(message)
      override def isActive: Boolean = MixNode.this.isActive
      override def trace: Trace =
        new Trace(Seq(new TraceNode(serial, "mix", None, None)))
      override def acceptInit(i: T): Unit = forwardInit(i)
    }

    override def accept(message: Try[T]): Unit = throw new IllegalArgumentException(
      "This not takes messages only through its intakes")

    override def acceptInit(i: T): Unit = throw new IllegalArgumentException(
      "This not takes messages only through its intakes")

    override def trace: Trace = new Trace(intake1.trace.paths
      .appendedAll(intake2.trace.paths))
  }


  private class DeltaFilterNode[T, S](init: Option[T], fn: T => S) extends AbstractNode[T, T](init) {
    override def accept(message: Try[T]): Unit =
      if(message.isFailure) {
        forward(message)
      } else if(!peek.map(fn).contains(fn(message.get))) {
        forward(message)
      }
    override def trace: Trace = trace("delta")
    override def acceptInit(i: T): Unit = {}
  }


  private class FlatMapNode[T, S](init: Option[T], fn: T => Flow[S])
    extends AbstractNode[T, S](init.flatMap(s => fn(s).peek))
  {
    override def accept(message: Try[T]): Unit = message match {
      case Success(t) => fn(t).foreach(s => forward(Success(s)))
      case Failure(f) => forward(Failure(f))
    }

    override def trace: Trace = trace("flat-map")

    override def acceptInit(i: T): Unit = {}
  }


  private class DeltaNode[T](init: Option[T])
    extends AbstractNode[T, Delta[T]](init.map(i => new Delta(None, i)))
  {
    private var lastInput: Option[T] = init
    override def accept(message: Try[T]): Unit = {
      forward(message.map(value => new Delta(lastInput, value)))
      message.foreach(value => lastInput = Some(value))
    }
    override def trace: Trace = trace("delta-map")
    override def acceptInit(i: T): Unit = forwardInit(new Delta(None, i))
  }


  private class MapNode[T, S](init: Option[S], name: String, fn: T => S)
    extends AbstractNode[T, S](init)
  {
    override def accept(message: Try[T]): Unit = forward(message.map(fn))
    override def trace: Trace = trace(name)
    override def acceptInit(i: T): Unit = forwardInit(fn(i))
  }


  private class JoinNode[T1, T2](t1: Flow[T1], t2: Flow[T2])
    extends AbstractNode[Unit, (T1, T2)](
      t1.peek.flatMap(
        i1 => t2.peek.map(i2 => (i1, i2))))
  {
    val intake1: Intake[T1] = new Intake[T1] {
      override def accept(message: Try[T1]): Unit = t2.peek.foreach(
        i2 => forward(message.map(v => (v, i2))))
      override def acceptInit(i: T1): Unit = t2.peek.foreach(
        i2 => forwardInit((i, i2)))
      override def isActive: Boolean = JoinNode.super.isActive
      override def trace: Trace = JoinNode.this.trace(s"join")
    }
    val intake2: Intake[T2] = new Intake[T2] {
      override def accept(message: Try[T2]): Unit = t1.peek.foreach(
        i1 => forward(message.map(v => (i1, v))))
      override def acceptInit(i: T2): Unit = t1.peek.foreach(
        i1 => forwardInit((i1, i)))
      override def isActive: Boolean = JoinNode.super.isActive
      override def trace: Trace =
        new Trace(Seq(new TraceNode(serial, "join", None, None)))
    }
    override def accept(message: Try[Unit]): Unit = throw new IllegalArgumentException(
      "This not takes messages only through its intakes")
    override def acceptInit(i: Unit): Unit = throw new IllegalArgumentException(
      "This not takes messages only through its intakes")
    override def trace: Trace = new Trace(intake1.trace.paths
      .appendedAll(intake2.trace.paths))
  }


  private class FilterNode[T](init: Option[T], predicate: T => Boolean)
    extends AbstractNode[T, T](init)
  {
    override def accept(message: Try[T]): Unit =
      if(message.isFailure) {
        forward(message)
      } else if(predicate(message.get)){
        forward(message)
      }
    override def trace: Trace = trace("filter")
    override def acceptInit(i: T): Unit =
      if(predicate(i)) {
        forwardInit(i)
      }
  }


  private class ForeachNode[T](init: Option[T], consumer: T => Unit)
    extends AbstractNode[T, Unit](None)
  {
    init.foreach(consumer)
    override def accept(message: Try[T]): Unit = message.foreach(consumer)
    override def isActive: Boolean = true
    override def trace: Trace = trace("foreach")
    override def acceptInit(i: T): Unit = consumer(i)
  }


  /**
   * Interface for inlet nodes. An inlet node provides an input that
   * can be connected to from another graph at a future time
   */
  trait Inlet[T] extends Flow[T] {
    /**
     * Convenience method for fluent notation.
     * This very Inlet object is given as input to fn.
     */
    def use(fn: Inlet[T] => Unit): Inlet[T] = {
      fn(this)
      this
    }

    /**
     * Convenience method for fluent notation. It is like foreach() method,
     * except its output retains the Inlet type.
     */
    def useForeach(fn: T => Unit): Inlet[T] = {
      foreach(fn)
      this
    }

    /**
     * Creates a Writable node that outputs into this Inlet node.
     */
    def createWriter(): Writable[T] = {
      val wr = writable[T]
      wr.forward(this)
      wr
    }
  }


  private class ForwardNode[T](init: Option[T], name: String)
    extends AbstractNode[T, T](init) with Inlet[T]
  {
    private var input: Option[AbstractNode[_, T]] = None
    def setInput(node: AbstractNode[_, T]): ForwardNode[T] = {
      input.foreach(_.removeOutput(this))
      input = Some(node)
      this
    }
    override def accept(message: Try[T]): Unit = forward(message)
    override def isActive: Boolean = input.nonEmpty || super.isActive
    override def trace: Trace = trace(name + (if(input.nonEmpty) "" else "!"))
    override def acceptInit(i: T): Unit = forwardInit(i)
  }


  private class FutureForwardNode[T, S](init: Option[T], fn: T => Future[S],
    ec: ExecutionContext)
    extends AbstractNode[T, S](None)
  {
    init.foreach(i => fn(i).onComplete(forward)(ec))
    override def accept(message: Try[T]): Unit = message.map(fn) match {
      case Success(f) => f.onComplete(forward)(ec)
      case Failure(e) => forward(Failure(e))
    }
    override def trace: Trace = trace("future-map")
    override def acceptInit(i: T): Unit = {}
  }


  private class OptionForwardNode[T, S](init: Option[T], fn: T => Option[S])
    extends AbstractNode[T, S](init.flatMap(fn))
  {
    override def accept(message: Try[T]): Unit = message.map(fn) match {
      case Success(Some(v)) =>  forward(Success(v))
      case Success(None) => /* Nothing */
      case Failure(e) => forward(Failure(e))
    }
    override def trace: Trace = trace("option-map")
    override def acceptInit(i: T): Unit = fn(i).foreach(forwardInit)
  }


  private class RecoverNode[T](init: Option[T], fn: PartialFunction[Throwable, T])
    extends AbstractNode[T, T](init)
  {
    override def accept(message: Try[T]): Unit = forward(message.recover(fn))
    override def trace: Trace = trace("recover")
    override def acceptInit(i: T): Unit = forwardInit(i)
  }


  private class ErrorsNode[T] extends AbstractNode[T, Throwable](None) {
    override def accept(message: Try[T]): Unit = if(message.isFailure) {
      forward(message.failed)
    }
    override def trace: Trace = trace("errors")
    override def acceptInit(i: T): Unit = {}
  }


  private class ForkErrorsNode[T](forward: ForwardNode[T], name: String)
    extends AbstractNode[T, Throwable](None)
  {
    override def accept(message: Try[T]): Unit = message match {
      case s: Success[T] => forward.accept(s)
      case Failure(e) => forward(Success(e))
    }
    override def trace: Trace = trace(name)
    override def acceptInit(i: T): Unit = forward.acceptInit(i)
  }


  private class ForeachErrorNode[T](init: Option[T], fn: Throwable => Unit)
    extends AbstractNode[T, T](init)
  {
    override def accept(message: Try[T]): Unit = message match {
      case s: Success[T] => forward(s)
      case Failure(e) => fn(e)
    }
    override def trace: Trace = trace("handle-errors")
    override def acceptInit(i: T): Unit = forwardInit(i)
  }


  /**
   * Interface for writable nodes. These are "producer" nodes that can be
   * written to using write() method.
   */
  trait Writable[T] extends Flow[T] {
    def write(message: T): Unit
    def write(futureMessage: Future[T])(implicit ec: ExecutionContext): Unit
    def step(fn: T => T): Unit
  }


  private class WritableNodeImpl[T](init: Option[T])
    extends AbstractNode[Unit, T](init) with Writable[T]
  {
    override def write(message: T): Unit = forward(Try(message))

    override def write(futureMessage: Future[T])(
        implicit ec: ExecutionContext): Unit =
      futureMessage.onComplete(forward)

    override def accept(message: Try[Unit]): Unit =
      throw new IllegalStateException("This node should not have been " +
        "positioned to receive messages")

    override def acceptInit(i: Unit): Unit = throw new IllegalStateException(
      "This node should not have been positioned to receive messages")

    override def trace: Trace = trace("writable")

    override def step(fn: T => T): Unit = peek.foreach(v => forward(Try(fn(v))))
  }


  private class ConstantNode[T](t: Option[T]) extends AbstractNode[Unit, T](t) {
    override def accept(message: Try[Unit]): Unit =
      throw new IllegalStateException("This node does not accept messages")
    override def acceptInit(i: Unit): Unit =
      throw new IllegalStateException("This node does not accept messages")
    override def trace: Trace = trace("constant")
  }


  /**
   * A Closed node would never produce any output.
   */
  trait Closed[T] extends Inlet[T]


  private class ClosedInlet[T]() extends AbstractNode[Unit, T](None)
    with Closed[T]
  {
    override def accept(message: Try[Unit]): Unit = {}
    override def acceptInit(i: Unit): Unit = {}
    override def trace: Trace = trace("closed")
    override def isActive: Boolean = false
  }


  private class FutureNode[T](init: Option[T], f: Future[T])(
      implicit ec: ExecutionContext)
    extends AbstractNode[Unit, T](init)
  {
    f.onComplete(forward)
    override def accept(message: Try[Unit]): Unit =
      throw new IllegalStateException("This node does not accept messages")
    override def acceptInit(i: Unit): Unit =
      throw new IllegalStateException("This node does not accept messages")
    override def trace: Trace = trace("future")
  }


  /**
   * A node in visualizable trace graph obtained via Flow.trace() method.
   */
  class TraceNode(val id: Int, val name: String, val value: Option[String],
    val next: Option[TraceNode])
  {
    def size: Int = 1 + next.map(_.size).getOrElse(0)
    override def toString: String = {
      val valuePart = value.map(", " + _).getOrElse("")
      f"$name($id%X$valuePart)" +
        next.map(" -> " + _.toString).getOrElse("")
    }
  }


  /**
   * Visualizable trace of a flow graph obtained using Flow.trace() method.
   */
  class Trace(val paths: Seq[TraceNode]) {
    def append(route: TraceNode): Trace = new Trace(paths.appended(route))
    def size: Int = paths.map(_.size).sum
    override def toString: String = paths.mkString("\n")
  }


  /** A Delta object carries two most temporally recent values.  */
  class Delta[T](val past: Option[T], val present: T)


  private val serialCounter = new AtomicInteger


  /**
   * Creates a flow producer node with a constant output.
   */
  def constant[T](value: T): Flow[T] = new ConstantNode(Some(value))


  /**
   * Creates a flow producer node that has no, or a constant output depending
   * of the `value` parameter.
   */
  def optionConstant[T](value: Option[T]): Flow[T] = new ConstantNode[T](value)


  /**
   * Creates an Inlet node that can be connected to at a later time.
   */
  def inlet[T]: Inlet[T] = new ForwardNode[T](None, "inlet")


  /**
   * Creates an Inlet node that outputs a constant value when not connected.
   */
  def inlet[T](init: T): Inlet[T] = new ForwardNode[T](Some(init), "inlet")


  /**
   * Creates an Inlet node that may produce a constant value when not connected,
   * depending on the `init` parameter.
   */
  def inlet[T](init: Option[T]): Inlet[T] = new ForwardNode[T](init, "inlet")


  /**
   * Creates a flow producer node that can be written to by calling its write()
   * method.
   */
  def writable[T]: Writable[T] = new WritableNodeImpl[T](None)


  /**
   * Creates a Writable node that has a constant output before it is written to.
   */
  def writable[T](init: T): Writable[T] = new WritableNodeImpl[T](Some(init))


  /**
   * Creates a Writable node that may have a constant output before it is
   * written to, depending on the value of `init` parameter.
   */
  def writable[T](init: Option[T]): Writable[T] = new WritableNodeImpl[T](init)


  /**
   * Creates a closed Inlet node. This can be used for inlet nodes accepted
   * as parameter, to indicate they are not an never will be used.
   */
  def closed[T]: Closed[T] = new ClosedInlet()


  /**
   * Creates a producer node that will output a constant value when the given
   * Future is ready.
   */
  def future[T](f: Future[T])(implicit ec: ExecutionContext): Flow[T] =
    new FutureNode[T](None, f)(ec)
}



/**
 * Common interface for nodes in a flow graph.
 * A flow graph is an asynchronous fixture of pipeline that transforms and
 * transfers data from one component of the program to another. Flow graph is
 * a stateful construct, meaning that, once a node in the graph outputs a value,
 * it will retain that value until it is changed. The retained value
 * can be queried using `peek()` method.
 */
trait Flow[T] extends {

  /**
   * If this not has not received any input yet, until it does, the output will
   * be set to the given value.
   */
  def default(init: T): Flow[T]


  /**
   * Adds a node to the graph that transforms its input using the given function.
   */
  def map[S](fn: T => S): Flow[S]


  /**
   * Adds a node to the graph that accepts futures as input, and
   * once each input is readable, transforms the value it carries using the
   * given function.
   */
  def futureMap[S](fn: T => Future[S])(implicit ec: ExecutionContext): Flow[S]


  /**
   * Adds an OptionMap node. If the input to this node is None, it does nothing.
   * Otherwise,it transforms the value carried by the input using the given
   * function.
   */
  def optionMap[S](fn: T => Option[S]): Flow[S]


  /**
   * Adds a filter node that passes only those items that satisfy the given
   * predicate.
   */
  def filter(predicate: T => Boolean): Flow[T]


  /**
   * Adds a node that merges items coming from this flow and the othe other one,
   * into a single flow.
   */
  def mix(other: Flow[T]): Flow[T]


  /**
   * Convenience method to construct a subgraph.
   */
  def remix(fn: Flow[T] => Flow[T]): Flow[T]


  /**
   * Adds a join node. When the value of the and inbound flow to a join node is
   * changed, given the other flow as a value, the output will be a tuple
   * made of the values of both inbound flows.
   */
  def join[S](other: Flow[S]): Flow[(T, S)]


  /**
   * Adds a consumer node.
   */
  def foreach(consumer: T => Unit): Flow[T]


  /**
   * Forwards the output of this flow to the input the given Inlet node.
   */
  def forward(node: Flow.Inlet[T]): Flow[T]


  /**
   * Adds a node that produces an output only when its input is changed. It does
   * nothing when repeatedly fed with the same input.
   */
  def whenChanged: Flow[T]


  /**
   * Adds a node that uses the given function to transform its input, and
   * produces an output when result is different than before. This node is
   * useful to detect changes in a part of the given input.
   */
  def whenChanged[S](fn: T => S): Flow[T]


  /**
   * Adds a node that produces Delta objects, containing the current value and
   * previous value of the input node.
   */
  def delta: Flow[Flow.Delta[T]]


  /**
   * Returns the last output produced by this node.
   * @return
   */
  def peek: Option[T]


  /**
   * Determines if this node has current a path forward to a consumer.
   * @return
   */
  def isActive: Boolean


  /**
   * Produces a visualizable trace of the graph beginning from this node.
   * @return
   */
  def trace: Flow.Trace


  /**
   * Adds a node that ignores healthy inputs and consumes only errors.
   */
  def handleErrors(fn: Throwable => Unit): Flow[T]


  /**
   * Splits this flow into two, redirecting all healthy inputs to one, and
   * all errors to another.
   */
  def splitErrors: (Flow[T], Flow[Throwable])


  /**
   * Adds a node that filters out healthy inputs and passes on all the errors.
   */
  def errors: Flow[Throwable]


  /**
   * Adds a node that transforms errors into healthy output.
   */
  def recover(fn: PartialFunction[Throwable, T]): Flow[T]


  /**
   * Used for development/debug purposes, forces all input nodes to pass as
   * active.
   */
  def forceActiveStatus(status: Boolean): Flow[T]
}