package net.kfoundation.js.util

import scala.scalajs.js
import scala.scalajs.js.PropertyDescriptor


object JSTools {

  def toMap(obj: js.Object): Map[String, Any] = js.Object
    .properties(obj)
    .flatMap(n => get(obj, n).map(v => (n, v)))
    .toMap

  def asUndefOr[T](value: T): js.UndefOr[T] = value

  def set(obj: js.Object, pName: String, pValue: Any): js.Object = {
    js.Object.defineProperty(obj, pName, new PropertyDescriptor {
      value = pValue
      writable = true
      configurable = true
      enumerable = true
    })
    obj
  }

  def get(obj: js.Object, pName: String): Option[Any] = js.Object
    .getOwnPropertyDescriptor(obj, pName)
    .value
    .toOption

}
