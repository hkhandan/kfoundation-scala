package net.kfoundation.js.serialization

import net.kfoundation.scala.serialization.ObjectDeserializer


trait ObjectDeserializerFactory {
  def newInstance(input: String): ObjectDeserializer
}
