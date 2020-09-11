package net.kfoundation.scala.serialization

import java.io.{ByteArrayOutputStream, OutputStream}

import net.kfoundation.scala.UString
import net.kfoundation.scala.io.Path



trait ValueWriter[T] {
  def write(serializer: ObjectSerializer, value: T): Unit


  def toString(factory: ObjectSerializerFactory, value: T): UString = {
    val output = new ByteArrayOutputStream()
    write(factory.of(output), value)
    val result = UString.of(output.toByteArray)
    output.close()
    result
  }


  def write(factory: ObjectSerializerFactory, output: OutputStream, value: T): Unit =
    write(factory.of(output), value)


  def write(factory: ObjectSerializerFactory, path: Path, value: T): Unit =
    write(factory.of(path.getOutputStream), value)


  def toWriterOf[S](implicit conversion: S => T): ValueWriter[S] =
    (serializer: ObjectSerializer, value: S) =>
      ValueWriter.this.write(serializer, conversion.apply(value))


  implicit def arrayWriter: ValueWriter[Array[T]] =
    (serializer: ObjectSerializer, value: Array[T]) => {
      serializer.writeCollectionBegin()
      value.foreach(write(serializer, _))
      serializer.writeCollectionEnd()
    }
}