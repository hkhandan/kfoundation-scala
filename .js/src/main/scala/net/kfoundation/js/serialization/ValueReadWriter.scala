package net.kfoundation.js.serialization

import java.io.ByteArrayOutputStream

import net.kfoundation.scala.{serialization => s}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}



@JSExportTopLevel("ValueReadWriter")
class ValueReadWriter[T](impl: s.ValueReadWriter[T]) {

  def getImplementation: s.ValueReadWriter[T] = impl

  @JSExport
  def read(factory: ObjectDeserializerFactory, input: String): T =
    impl.read(factory.newInstance(input))

  @JSExport
  def write(factory: ObjectSerializerFactory, value: T): String = {
    val output = new ByteArrayOutputStream()
    impl.write(factory.newInstance(output), value)
    output.toString()
  }

}
