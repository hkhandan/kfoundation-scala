package net.kfoundation.scala.serialization

import net.kfoundation.scala.UString
import net.kfoundation.scala.parse.CodeLocation
import net.kfoundation.scala.parse.lex._

import scala.annotation.tailrec



abstract class ObjectDeserializer {

  def readObjectBegin(): Option[UString]
  def readObjectEnd(): Option[UString]
  def readCollectionBegin(): Unit
  def tryReadCollectionEnd(): Boolean
  def tryReadPropertyName(): Option[UString]
  def readStringLiteral(): UString
  def readIntegerLiteral(): Long
  def readDecimalLiteral(): Double
  def readBooleanLiteral(): Boolean
  protected def getCurrentLocation: CodeLocation


  def readLiteralOrString[T](cls: Class[T]): Either[T, UString] =
    Right(readStringLiteral())


  def readLiteralOrInteger[T](cls: Class[T]): Either[T, Long] =
    Right(readIntegerLiteral())


  def readLiteralOrDecimal[T](cls: Class[T]): Either[T, Double] =
    Right(readDecimalLiteral())


  def readObjectBegin(expectedName: UString): Unit = readObjectBegin()
    .foreach(name => if(!name.equals(expectedName))
      throw new DeserializationError(
        s"At $getCurrentLocation object of type $expectedName was expected but found: $name"))


  def readPropertyName(): UString = tryReadPropertyName()
    .getOrElse(throw new LexicalError(
      getCurrentLocation,
      "Missing expected property name"))


  def readObject(typeName: UString, fn: UString => Unit): Unit = {
    readObjectBegin(typeName)

    @tailrec
    def readNextProps(): Unit = {
      val maybeProp = tryReadPropertyName()
      if(maybeProp.isDefined) {
        fn(maybeProp.get)
        readNextProps()
      }
    }

    readNextProps()
    readObjectEnd()
  }

}
