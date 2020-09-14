package net.kfoundation.scala.serialization

import net.kfoundation.scala.UString

import scala.annotation.tailrec
import scala.collection.mutable



object ValueReadWriter {
  // TODO move these to generated code
  class FlexObjectWriter(
    typeName: UString,
    properties: Map[UString, ValueReadWriter[Any]])
    extends ValueWriter[Map[UString, Any]]
  {
    override def write(serializer: ObjectSerializer, value: Map[UString, Any]): Unit = {
      serializer.writeObjectBegin(typeName)
      value.foreach(kv => properties.get(kv._1)
        .getOrElse(throw new SerializationError(s"""No writer provided for property "${kv._1}" of $typeName"""))
        .write(serializer, kv._2))
      serializer.writeObjectEnd()
    }
  }


  class FlexObjectReader(
    typeName: UString,
    properties: Map[UString, ValueReadWriter[Any]])
    extends ValueReader[Map[UString, Any]]
  {
    override def read(deserializer: ObjectDeserializer): Map[UString, Any] = {
      val result = new mutable.HashMap[UString, Any]()

      @tailrec
      def loop(): Unit = {
        val pToken = deserializer.tryReadPropertyName()
        if(pToken.isDefined) {
          val pName = pToken.get
          val reader = properties.getOrElse(pName, throw new DeserializationError(
            "Reader for property is not provided: " + typeName + "." + pName))
          result.put(pName, reader.read(deserializer))
          loop()
        }
      }

      deserializer.readObjectBegin(typeName)
      loop()
      deserializer.readObjectEnd()

      result.toMap
    }
  }


  class FlexObjectReaderWriter(
    typeName: UString,
    properties: Map[UString, ValueReadWriter[Any]])
    extends ValueReadWriter[Map[UString, Any]]
  {
    private val reader = new FlexObjectReader(typeName, properties)
    private val writer = new FlexObjectWriter(typeName, properties)

    override def write(serializer: ObjectSerializer, value: Map[UString, Any]): Unit =
      writer.write(serializer, value)

    override def read(deserializer: ObjectDeserializer): Map[UString, Any] =
      reader.read(deserializer)
  }
}



trait ValueReadWriter[T] extends ValueReader[T] with ValueWriter[T] {

  def toReadWriterOf[S](implicit toConversion: T => S, fromConversion: S => T):
  ValueReadWriter[S] =
    new ValueReadWriter[S] {
      override def write(serializer: ObjectSerializer, value: S): Unit =
        ValueReadWriter.this.write(serializer, fromConversion(value))
      override def read(deserializer: ObjectDeserializer): S =
        toConversion(ValueReadWriter.this.read(deserializer))
    }


  def seq: ValueReadWriter[Seq[T]] = new ValueReadWriter[Seq[T]] {
    override def write(serializer: ObjectSerializer, value: Seq[T]): Unit = {
      serializer.writeCollectionBegin()
      value.foreach(item => ValueReadWriter.this.write(serializer, item))
      serializer.writeCollectionEnd()
    }

    override def read(deserializer: ObjectDeserializer): Seq[T] = {
      var result = Seq[T]()
      deserializer.readCollectionBegin()
      while(!deserializer.tryReadCollectionEnd()) {
        result = result :+ ValueReadWriter.this.read(deserializer)
      }
      result
    }
  }

}