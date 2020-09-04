package net.kfoundation.scala.serialization

trait ValueWriter[T] {
  def write(serializer: ObjectSerializer, value: T): Unit

  def toWriterOf[S](implicit conversion: S => T): ValueWriter[S] =
    (serializer: ObjectSerializer, value: S) =>
      ValueWriter.this.write(serializer, conversion.apply(value))
}