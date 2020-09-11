package net.kfoundation.scala.serialization

import java.io.InputStream

import net.kfoundation.scala.UString
import net.kfoundation.scala.io.Path



trait ValueReader[T] {
  def read(deserializer: ObjectDeserializer): T


  def read(factory: ObjectDeserializerFactory, str: UString): T =
    read(factory.of(str))


  def read(factory: ObjectDeserializerFactory, input: InputStream): T =
    read(factory.of(input))


  def read(factory: ObjectDeserializerFactory, path: Path): T =
    read(factory.of(path))


  def getDefaultValue: T =
    throw new DeserializationError("Value is missing and no default is available")


  def toReaderOf[S](implicit conversion: T => S): ValueReader[S] =
    (deserializer: ObjectDeserializer) =>
      conversion.apply(ValueReader.this.read(deserializer))


  def toSeqReader: ValueReader[Seq[T]] = d => {
    d.readCollectionBegin()
    var values = List[T]()
    while(d.tryReadCollectionEnd()) {
      values = values :+ ValueReader.this.read(d)
    }
    values
  }


  def toOptionReader: ValueReader[Option[T]] = new ValueReader[Option[T]] {
    override def read(deserializer: ObjectDeserializer): Option[T] =
      Some(ValueReader.this.read(deserializer))
    override def getDefaultValue: Option[T] = None
  }
}