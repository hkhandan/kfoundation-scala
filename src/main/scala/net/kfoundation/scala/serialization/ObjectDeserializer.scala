// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization

import net.kfoundation.scala.UString
import net.kfoundation.scala.parse.CodeLocation
import net.kfoundation.scala.parse.lex._



/**
 * Abstract framework for a deserializer that transform a stream of characters
 * into a stream of objects.
 */
abstract class ObjectDeserializer {

  /**
   * Reads the beginning of an object, otherwise throws an error.
   * @return the type name of the object if the input provides on,
   *         otherwise, None.
   */
  def readObjectBegin(): Option[UString]


  /**
   * Reads the end of an object, otherwise throws an error.
   * @return the type name of the object if provided by input, otherwise, None.
   */
  def readObjectEnd(): Option[UString]


  /**
   * Reads the beginning of a collection, otherwise throws an error.
   */
  def readCollectionBegin(): Unit


  /**
   * Attempts to read the end of a collection, returns true of successful.
   * @return whether or not collection end could be read.
   */
  def tryReadCollectionEnd(): Boolean


  /**
   * Attempts to read a property name (property definition).
   *
   * @return If successful, the name of property read, otherwise None.
   */
  def tryReadPropertyName(): Option[UString]


  /**
   * Reads and returns the value of the string literal next in the input stream.
   * Throws an exception if one could not be found.
   */
  def readStringLiteral(): UString


  /**
   * Reads and returns the value of the integral literal next in the input stream.
   * Throws an exception if one could not be found.
   */
  def readIntegerLiteral(): Long


  /**
   * Reads and returns the value of a possibly fractional decimal literal next
   * in the input stream. Throws an exception if one could not be found.
   */
  def readDecimalLiteral(): Double


  /**
   * Reads and returns the value of boolean literal next
   * in the input stream. Throws an exception if one could not be found.
   *
   * @return
   */
  def readBooleanLiteral(): Boolean


  /**
   * Returns the CodeLocation right after the last successfully read one.
   */
  protected def getCurrentLocation: CodeLocation


  /**
   * Attempt to read the null literal next from the input stream.
   */
  def tryReadNullLiteral(): Boolean =
    throw new DeserializationError(
      "Reading null is not supported in this deserializer")


  /**
   * Reader for custom literal, with string fallback. If this deserializer
   * supports the given class the return value will be of that type, otherwise
   * it will be a string.
   */
  def readLiteralOrString[T](cls: Class[T]): Either[T, UString] =
    Right(readStringLiteral())


  /**
   * Reader for custom literal, with integer fallback. If this deserializer
   * supports the given class the return value will be of that type, otherwise
   * it will be an integer.
   */
  def readLiteralOrInteger[T](cls: Class[T]): Either[T, Long] =
    Right(readIntegerLiteral())


  /**
   * Reader for custom literal, with fractional decimal fallback. If this
   * deserializer supports the given class the return value will be of that
   * type, otherwise it will be a string.
   */
  def readLiteralOrDecimal[T](cls: Class[T]): Either[T, Double] =
    Right(readDecimalLiteral())


  /**
   * Read the beginning of an object and tests if its name is the same as
   * the one provided. If name does not match, it throws an error.
   */
  def readObjectBegin(expectedName: UString): Unit = readObjectBegin()
    .foreach(name => if(!name.equals(expectedName))
      throw new DeserializationError(
        s"${getCurrentLocation.getLocationTag} object of type $expectedName was expected but found: $name"))


  /**
   * Read the property name next in the stream, if none found, throws an
   * exception.
   */
  def readPropertyName(): UString = tryReadPropertyName()
    .getOrElse(throw new LexicalError(
      getCurrentLocation,
      "Missing expected property name"))

}
