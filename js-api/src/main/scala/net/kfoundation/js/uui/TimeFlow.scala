package net.kfoundation.js.uui

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.scalajs.js.timers._



object TimeFlow {
  trait Window {
    def cancel(): Window
    def andThen(duration: Duration)(fn: Float => Unit): Window
    def reverse(): Window
    def isFinished: Boolean
  }

  private val STEP_INTERVAL: Int = 1000/32

  val GLOBAL = new TimeFlow
}


class TimeFlow {
  import TimeFlow._

  class AlphaInterpolatorImpl(start: Int, end: Int, fn: Float => Unit)
    extends Window
  {
    private var _isFinished = false
    private val scale = (end - start).toFloat

    def set(time: Int): Unit = if(!_isFinished && time >= start) {
      fn((time - start)/scale)
      _isFinished = time >= end
    }

    override def cancel(): Window = {
      _isFinished = true
      this
    }

    override def andThen(duration: Duration)(fn: Float => Unit):
    Window = add(
      if(end > ticks && !_isFinished) end else ticks,
      duration.toMillis.toInt,
      fn)

    override def reverse(): Window = {
      cancel()
      val newScale = (ticks-start)/scale
      add(ticks, ticks-start, d => fn(d*newScale))
    }

    override def isFinished: Boolean = _isFinished
  }


  var ticks: Int = 0
  var timer: Option[SetIntervalHandle] = None
  var interpolators = new ListBuffer[AlphaInterpolatorImpl]


  private def add(start: Int, duration: Int, fn: Float => Unit):
    AlphaInterpolatorImpl =
  {
    val ip = new AlphaInterpolatorImpl(start,
      start + duration/STEP_INTERVAL, fn)
    interpolators += ip
    if(timer.isEmpty) {
      timer = Some(setInterval(STEP_INTERVAL)(tick()))
    }
    ip
  }

  def start(duration: Duration)(fn: Float => Unit): Window =
    add(ticks, duration.toMillis.toInt, fn)

  def empty(): Window = new Window {
    override def cancel(): Window = this

    override def andThen(duration: Duration)(fn: Float => Unit):
    Window = TimeFlow.this.start(duration)(fn)

    override def reverse(): Window = this

    override def isFinished: Boolean = true
  }

  def tick(): Unit = {
    ticks += 1
    interpolators.foreach(_.set(ticks))
    if(ticks % 100 == 0) {
      interpolators = interpolators.filter(!_.isFinished)
      if(interpolators.isEmpty) {
        clearInterval(timer.get)
        timer = None
      }
    }
  }
}
