package net.kfoundation.js.serialization

import net.kfoundation.scala.{UString, serialization => s}

import java.io.ByteArrayInputStream
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.util.Using



@JSExportTopLevel("Deserializers")
object Deserializers {
  private def adapt(input: String, factory: s.ObjectDeserializerFactory):
      s.ObjectDeserializer =
    Using(
      new ByteArrayInputStream(UString.of(input).toUtf8))(
      factory.of(_))
    .get

  @JSExport
  val JSON: ObjectDeserializerFactory =
    (input: String) => adapt(input, s.JsonObjectDeserializer.FACTORY)

  @JSExport
  val K4: ObjectDeserializerFactory =
    (input: String) => adapt(input, s.K4ObjectDeserializer.FACTORY)

  @JSExport
  val XML: ObjectDeserializerFactory =
    (input: String) => adapt(input, s.XmlObjectDeserializer.FACTORY)
}