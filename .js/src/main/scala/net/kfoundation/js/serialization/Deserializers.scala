package net.kfoundation.js.serialization

import net.kfoundation.scala.{serialization => s}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}



@JSExportTopLevel("Deserializers")
object Deserializers {

  @JSExport
  val JSON: ObjectDeserializerFactory =
    (input: String) => s.JsonObjectDeserializer.FACTORY.of(input)

  @JSExport
  val K4: ObjectDeserializerFactory =
    (input: String) => s.K4ObjectDeserializer.FACTORY.of(input)

  @JSExport
  val XML: ObjectDeserializerFactory =
    (input: String) => s.XmlObjectDeserializer.FACTORY.of(input)

}