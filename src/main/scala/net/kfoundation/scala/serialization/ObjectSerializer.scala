package net.kfoundation.scala.serialization

import net.kfoundation.scala.UString


object ObjectSerializer {

}


abstract class ObjectSerializer {

  def writePropertyName(name: UString): ObjectSerializer
  def writeLiteral(value: UString): ObjectSerializer
  def writeLiteral(value: Long): ObjectSerializer
  def writeLiteral(value: Double): ObjectSerializer
  def writeLiteral(value: Boolean): ObjectSerializer
  def writeNull(): ObjectSerializer
  def writeObjectBegin(name: UString): ObjectSerializer
  def writeObjectEnd(): ObjectSerializer
  def writeCollectionBegin(): ObjectSerializer
  def writeCollectionEnd(): ObjectSerializer
  def writeStreamEnd(): Unit

  def writeValue[T](value: T)(implicit writer: ValueWriter[T]): ObjectSerializer = {
    writer.write(this, value)
    this
  }
}
