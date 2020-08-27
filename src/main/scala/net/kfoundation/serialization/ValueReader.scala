package net.kfoundation.serialization


trait ValueReader[T] {
  def read(deserializer: ObjectDeserializer): T

  def getDefaultValue: T = throw new DeserializationError("Value is missing and no default is available")

  def mapReader[S](implicit conversion: T => S): ValueReader[S] =
    (deserializer: ObjectDeserializer) =>
      conversion.apply(ValueReader.this.read(deserializer))

  def seqReader: ValueReader[Seq[T]] = d => {
    d.readCollectionBegin()
    var values = List[T]()
    while(d.tryReadCollectionEnd().isDefined) {
      values = values :+ ValueReader.this.read(d)
    }
    values
  }

  def optionReader: ValueReader[Option[T]] = new ValueReader[Option[T]] {
    override def read(deserializer: ObjectDeserializer): Option[T] =
      Some(ValueReader.this.read(deserializer))
    override def getDefaultValue: Option[T] = None
  }
}
