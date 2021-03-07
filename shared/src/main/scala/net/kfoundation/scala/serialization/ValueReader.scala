// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization

import java.io.{BufferedInputStream, ByteArrayInputStream, InputStream}
import net.kfoundation.scala.UString
import net.kfoundation.scala.io.Path

import scala.util.Using



/**
 * Used to implement the capability of deserializing values of type T.
 * @tparam T type of values to be read.
 */
trait ValueReader[T] {

  /** Reads value of T from given input, throws an error if failed. */
  def read(deserializer: ObjectDeserializer): T


  /**
   * Parses the given string for a value of type T, using the deserializer
   * produced by the given factory.
   */
  def read(factory: ObjectDeserializerFactory, str: UString): T =
    Using(new ByteArrayInputStream(str.toUtf8))(
      stream => read(factory.of(stream)))
    .get


  /**
   * Parses the given InputStream for a value of type T, using the
   * deserializer produced by the given factory.
   */
  def read(factory: ObjectDeserializerFactory, input: InputStream): T =
    if(input.markSupported()) {
      read(factory.of(input))
    } else {
      readBuffered(factory, input)
    }


  private def readBuffered(factory: ObjectDeserializerFactory, input: InputStream): T =
    Using(
      new BufferedInputStream(input))(
      s => read(factory.of(s)))
      .get


  /**
   * Parses the file pointed to by the given path for a value of type T, using
   * the deserializer produced by the given factory.
   */
  def read(factory: ObjectDeserializerFactory, path: Path): T =
    Using(
      path.newInputStream)(
      stream => read(factory, stream))
      .get


  /**
   * If an object in the stream being read is expected to have a property of
   * type T, but such property is missing, the value returned by this method
   * will be used instead. Default behavior of this method is not to allow
   * such properties to be missing by throwing an exception.
   */
  def getDefaultValue: T =
    throw new DeserializationError("Value is missing and no default is available")


  /**
   * Maps value read by this object to a value of type S.
   */
  def mapReader[S](implicit conversion: T => S): ValueReader[S] =
    (deserializer: ObjectDeserializer) =>
      conversion.apply(ValueReader.this.read(deserializer))


  /**
   * Produces a reader to read sequence of values of type T.
   */
  def seqReader: ValueReader[Seq[T]] = d => {
    d.readCollectionBegin()
    var values = List[T]()
    while(!d.tryReadCollectionEnd()) {
      values = values :+ ValueReader.this.read(d)
    }
    values
  }


  /**
   * Produces a reader to read a value of type T, with default for missing
   * value being None (rather than throwing an error).
   */
  def optionReader: ValueReader[Option[T]] = new ValueReader[Option[T]] {
    override def read(deserializer: ObjectDeserializer): Option[T] =
      Some(ValueReader.this.read(deserializer))
    override def getDefaultValue: Option[T] = None
  }


  def nullableReader: ValueReader[Option[T]] = (deserializer: ObjectDeserializer) =>
    if (deserializer.tryReadNullLiteral())
      None
    else
      Some(ValueReader.this.read(deserializer))

}