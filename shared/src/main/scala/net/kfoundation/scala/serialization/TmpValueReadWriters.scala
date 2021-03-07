package net.kfoundation.scala.serialization


object TmpValueReadWriters {
  import ValueReadWriters._

  def enumeration(e: Enumeration): ValueReadWriter[e.Value] = new ValueReadWriter[e.Value] {
    override def read(deserializer: ObjectDeserializer): e.Value =
      e.withName(deserializer.readStringLiteral().toString)

    override def write(serializer: ObjectSerializer, value: e.Value): Unit =
      serializer.writeLiteral(value.toString)
  }


  def map[K, V](implicit keyRW: ValueReadWriter[K],
      valueRW: ValueReadWriter[V]): ValueReadWriter[Map[K, V]] =
    tuple("Map", "entries" ->
      tuple("Pair",
        "key" -> keyRW,
        "value" -> valueRW)
      .seq)
    .mapRW(_.toMap, _.toSeq)

}
