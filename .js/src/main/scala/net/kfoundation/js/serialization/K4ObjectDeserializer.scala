package net.kfoundation.js.serialization

import net.kfoundation.scala.{serialization => s}

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("K4ObjectDeserializer")
class K4ObjectDeserializer(input: String) extends ObjectDeserializer {

  override def getImplementation: s.ObjectDeserializer =
    new s.K4ObjectDeserializer(input)

}
