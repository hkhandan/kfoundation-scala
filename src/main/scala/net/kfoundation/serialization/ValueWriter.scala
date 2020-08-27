package net.kfoundation.serialization

trait ValueWriter[T] {
  def write(serializer: ObjectSerializer, value: T): Unit

  def asWriterOf[S](implicit conversion: S => T): ValueWriter[S] =
    (serializer: ObjectSerializer, value: S) =>
      ValueWriter.this.write(serializer, conversion.apply(value))
}