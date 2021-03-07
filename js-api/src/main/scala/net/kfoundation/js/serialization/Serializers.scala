package net.kfoundation.js.serialization

import net.kfoundation.scala.{serialization => s}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}



@JSExportTopLevel("Serializers")
object Serializers {
  @JSExport
  val JSON: ObjectSerializerFactory = s.JsonObjectSerializer.FACTORY.of(_, 2, false)

  @JSExport
  val K4: ObjectSerializerFactory = s.K4ObjectSerializer.FACTORY.of(_, 2, false)

  @JSExport
  val XML: ObjectSerializerFactory = s.XmlObjectSerializer.FACTORY.of(_, 2, false)

  @JSExport
  val YAML: ObjectSerializerFactory = s.YamlObjectSerializer.FACTORY.of(_, 2, false)
}
