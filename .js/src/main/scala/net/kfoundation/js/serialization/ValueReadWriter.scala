package net.kfoundation.js.serialization

import net.kfoundation.scala.{serialization => s}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}


@JSExportTopLevel("ValueReadWriter")
class ValueReadWriter[T](impl: s.ValueReadWriter[T]) {

  def getImplementation: s.ValueReadWriter[T] = impl

  @JSExport
  def read(deserializer: ObjectDeserializer): T =
    impl.read(deserializer.getImplementation)

  @JSExport
  def write(serializer: ObjectSerializer, value: T): Unit =
    impl.write(serializer.getImplementation, value)

}
