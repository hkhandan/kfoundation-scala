package net.kfoundation.js.serialization

import net.kfoundation.scala.serialization._

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}



@JSExportTopLevel("Serializers")
object Serializers {

  @JSExport
  val JSON: ObjectSerializerFactory = JsonObjectSerializer.of(_)

  @JSExport
  val K4: ObjectSerializerFactory = K4ObjectSerializer.of(_)

  @JSExport
  val XML: ObjectSerializerFactory = XmlObjectSerializer.of(_)

  @JSExport
  val YAML: ObjectSerializerFactory = YamlObjectSerializer.of(_)

}
