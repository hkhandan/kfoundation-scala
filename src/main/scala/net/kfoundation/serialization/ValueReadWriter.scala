package net.kfoundation.serialization


trait ValueReadWriter[T] extends ValueReader[T] with ValueWriter[T] {

  def asReadWriterOf[S](implicit toConversion: T => S, fromConversion: S => T):
  ValueReadWriter[S] =
    new ValueReadWriter[S] {
      override def write(serializer: ObjectSerializer, value: S): Unit =
        ValueReadWriter.this.write(serializer, fromConversion(value))
      override def read(deserializer: ObjectDeserializer): S =
        toConversion(ValueReadWriter.this.read(deserializer))
    }

}
