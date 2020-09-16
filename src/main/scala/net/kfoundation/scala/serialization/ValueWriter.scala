// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization

import java.io.{ByteArrayOutputStream, OutputStream}

import net.kfoundation.scala.UString
import net.kfoundation.scala.io.Path



/**
 * Used to implement the capability of serializing values of type T.
 * @tparam T type of values to be read.
 */
trait ValueWriter[T] {

  /**
   * Writes a value of type T using the given serializer.
   */
  def write(serializer: ObjectSerializer, value: T): Unit


  /**
   * Converts the given value to string using the serializer produced by
   * the given factory.
   */
  def toString(factory: ObjectSerializerFactory, value: T): UString = {
    val output = new ByteArrayOutputStream()
    write(factory.of(output), value)
    val result = UString.of(output.toByteArray)
    output.close()
    result
  }


  /**
   * Writer a value of type T to the given OutputStream using the serializer
   * obtained from the given factory.
   */
  def write(factory: ObjectSerializerFactory, output: OutputStream, value: T): Unit =
    write(factory.of(output), value)


  /**
   * Writes a value of type to a file using the serializer obtained from the
   * given factory.
   */
  def write(factory: ObjectSerializerFactory, path: Path, value: T): Unit =
    write(factory.of(path.getOutputStream), value)


  /**
   * Given a mapping from type S to T, produces a writer that can write values
   * of type S.
   */
  def map[S](implicit conversion: S => T): ValueWriter[S] =
    (serializer: ObjectSerializer, value: S) =>
      ValueWriter.this.write(serializer, conversion.apply(value))


  /**
   * Produces a writer to serialize sequence of T.
   */
  implicit def seqWriter: ValueWriter[Seq[T]] =
    (serializer: ObjectSerializer, value: Seq[T]) => {
      serializer.writeCollectionBegin()
      value.foreach(write(serializer, _))
      serializer.writeCollectionEnd()
    }

}