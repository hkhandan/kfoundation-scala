package net.kfoundation.js.uui

import net.kfoundation.scala.util.Flow
import net.kfoundation.scala.uui.Color

import scala.concurrent.duration.FiniteDuration


object Animator {
  def range(from: Double, to: Double, alpha: Double): Double =
    from + (to - from)*alpha

  def rgba(from: Color, to: Color, alpha: Double): Color.RgbaColor = {
    val fromRgba = from.asRgba
    val toRgba = to.asRgba
    Color.rgba(
      range(fromRgba.red, toRgba.red, alpha),
      range(fromRgba.green, toRgba.green, alpha),
      range(fromRgba.blue, toRgba.blue, alpha),
      range(fromRgba.alpha, toRgba.alpha, alpha))
  }

  def blink(count: Int, alpha: Double): Boolean =
    Math.floor(alpha * count * 2) % 2 == 1

  def triangle(alpha: Double): Double =
    if(alpha < 0.5) {
      alpha*2
    } else {
      (1 - alpha)*2
    }
}


class Animator(duration: FiniteDuration, init: Double = 0) {
  private var window = TimeFlow.GLOBAL.empty()

  private val _outlet: Flow.Writable[Double] = Flow.writable[Double](init)

  def outlet: Flow[Double] = _outlet

  def play(): Unit = window = window.cancel()
      .andThen(duration)(_outlet.write(_))

  def seek(location: Double): Unit = {
    _outlet.write(location)
  }

  def forward(): Unit = {
    val alpha: Double = _outlet.peek.getOrElse(0)
    if(alpha < 1) {
      val scale = 1 - alpha
      val subDuration = duration * scale
      window = window.cancel()
        .andThen(subDuration)(d => _outlet.write(alpha + d*scale))
    }
  }

  def backward(): Unit = {
    val alpha: Double = _outlet.peek.getOrElse(1)
    if(alpha > 0) {
      val subDuration = duration * alpha
      window = window.cancel()
        .andThen(subDuration)(d => _outlet.write(alpha - d*alpha))
    }
  }

}
