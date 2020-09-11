package net.kfoundation.js.serialization

import net.kfoundation.scala.serialization._

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}



@JSExportTopLevel("Deserializers")
object Deserializers {

  @JSExport
  val JSON: ObjectDeserializerFactory =
    (input: String) => JsonObjectDeserializer.of(input)

  @JSExport
  val K4: ObjectDeserializerFactory =
    (input: String) => K4ObjectDeserializer.of(input)

  @JSExport
  val XML: ObjectDeserializerFactory =
    (input: String) => XmlObjectDeserializer.of(input)

}