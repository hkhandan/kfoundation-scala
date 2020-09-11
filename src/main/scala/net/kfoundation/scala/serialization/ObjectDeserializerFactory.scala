package net.kfoundation.scala.serialization

import java.io.InputStream

import net.kfoundation.scala.UString
import net.kfoundation.scala.io.Path


trait ObjectDeserializerFactory {
  def of(str: UString): ObjectDeserializer
  def of(input: InputStream): ObjectDeserializer
  def of(path: Path): ObjectDeserializer
}
