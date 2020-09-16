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



/**
 * Abstract framework for object serializers. This class is designed with
 * "fluent" API.
 */
abstract class ObjectSerializer {

  /** Writes a property name to the output. */
  def writePropertyName(name: UString): ObjectSerializer


  /** Writes a string literal to the output. */
  def writeLiteral(value: UString): ObjectSerializer


  /** Writes an integer literal to the output. */
  def writeLiteral(value: Long): ObjectSerializer


  /** Writes a possibly fractional decimal literal to the output. */
  def writeLiteral(value: Double): ObjectSerializer


  /** Writes a boolean value to the output. */
  def writeLiteral(value: Boolean): ObjectSerializer


  /** Writes symbol for null to the output. */
  def writeNull(): ObjectSerializer


  /** Writes the opening part of an object. */
  def writeObjectBegin(name: UString): ObjectSerializer


  /** Writes the closing part of an object. */
  def writeObjectEnd(): ObjectSerializer


  /** Writes the opening part of a collection. */
  def writeCollectionBegin(): ObjectSerializer


  /** Writes the closing part of a collection. */
  def writeCollectionEnd(): ObjectSerializer


  /** Ends the current stream (if applicable). */
  def writeStreamEnd(): Unit


  /**
   * Writes a literal of custom type with string fallback. If this serializer
   * does not directly support the given literal, it uses the given fallback
   * function to produce and write its string equivalent.
   */
  def writeLiteralOrString[T](value: T, fallback: () => UString): ObjectSerializer =
    writeLiteral(fallback())


  /**
   * Writes a literal of custom type with integer fallback. If this serializer
   * does not directly support the given literal, it uses the given fallback
   * function to produce and write its integral equivalent
   */
  def writeLiteralOrInteger[T](value: T, fallback: () => Long): ObjectSerializer =
    writeLiteral(fallback())


  /**
   * Writes a literal of custom type with integer fallback. If this serializer
   * does not directly support the given literal, it uses the given fallback
   * function to produce and write its numeric equivalent
   */
  def writeLiteralOrDecimal[T](value: T, fallback: () => Double): ObjectSerializer =
    writeLiteral(fallback())


  /**
   * Writes given value if a writer for it is available. This method helps
   * using this class in fluent form better.
   */
  def writeValue[T](value: T)(implicit writer: ValueWriter[T]): ObjectSerializer = {
    writer.write(this, value)
    this
  }

}