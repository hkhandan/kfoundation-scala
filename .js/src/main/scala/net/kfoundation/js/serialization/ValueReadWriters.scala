package net.kfoundation.js.serialization

import net.kfoundation.scala.serialization.SerializationError
import net.kfoundation.scala.{UString, serialization => s}
import net.kfoundation.js.util.JSTools

import scala.annotation.tailrec
import scala.scalajs.js
import scala.scalajs.js.JavaScriptException
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}


@JSExportTopLevel("ValueReadWriters")
object ValueReadWriters {

  class ObjectReadWriter(
    typeName: String,
    properties: Map[String, ValueReadWriter[Any]])
    extends s.ValueReadWriter[js.Object]
  {
    val rw = new s.ValueReadWriter.FlexObjectReaderWriter(
      typeName,
      properties.map(kv => (
        UString.of(kv._1),
        kv._2.getImplementation)))

    override def write(serializer: s.ObjectSerializer, value: js.Object): Unit =
      rw.write(serializer, js.Object.getOwnPropertyNames(value)
        .map(p => (
          UString.of(p),
          js.Object.getOwnPropertyDescriptor(value, p).value))
        .toMap)

    override def read(deserializer: s.ObjectDeserializer): js.Object = {
      val x = rw.read(deserializer)
      x.foldLeft(
        new js.Object())(
        (z, kv) =>  JSTools.set(z, kv._1.toString, kv._2))
    }

    override def toString: String = s"js.ValueReadWriter(typeName=$typeName, properties=$properties)"
  }

  class ArrayReadWriter(rw: ValueReadWriter[js.Any])
    extends s.ValueReadWriter[js.Array[js.Any]]
  {
    override def write(serializer: s.ObjectSerializer, value: js.Array[js.Any]): Unit = {
      if(!js.Array.isArray(value)) {
        throw new SerializationError("Expected an array, but provided: " + value)
      }
      serializer.writeCollectionBegin()
      value.foreach(i => rw.getImplementation.write(serializer, i))
      serializer.writeCollectionEnd()
    }

    override def read(deserializer: s.ObjectDeserializer): js.Array[js.Any] =  {
      var list = Seq[js.Object]()

      @tailrec
      def readNext(): Unit = if(deserializer.tryReadCollectionEnd().isEmpty) {
        list = list :+ rw.getImplementation.read(deserializer).asInstanceOf
        readNext()
      }

      deserializer.readCollectionBegin()
      readNext()

      js.Array(list:_*)
    }
  }

  private val STRING_IMPL = new s.ValueReadWriter[String] {
    override def read(deserializer: s.ObjectDeserializer): String =
      deserializer.readStringLiteral().value.toString

    override def write(serializer: s.ObjectSerializer, value: String): Unit =
      serializer.writeLiteral(UString.of(value))
  }

  @JSExport
  val STRING: ValueReadWriter[String] = new ValueReadWriter[String](STRING_IMPL)

  @JSExport
  val NUMBER: ValueReadWriter[Double] =
    new ValueReadWriter[Double](s.ValueReadWriters.DOUBLE)

  @JSExport
  val BOOLEAN: ValueReadWriter[Boolean] =
    new ValueReadWriter[Boolean](s.ValueReadWriters.BOOLEAN)

  @JSExport
  def ofArray(rw: ValueReadWriter[js.Any]): ValueReadWriter[js.Array[js.Any]] =
    new ValueReadWriter[js.Array[js.Any]](new ArrayReadWriter(rw))

  @JSExport
  def ofObject(typeName: String, properties: js.Object): ValueReadWriter[js.Object] =
    new ValueReadWriter[js.Object](
      new ObjectReadWriter(typeName, JSTools.toMap(properties).map(kv => kv._2 match {
        case x: ValueReadWriter[_] => (kv._1, x.asInstanceOf[ValueReadWriter[Any]])
        case _ => throw JavaScriptException(s"Expected a ValueReadWriter be provided for ${kv._1}, but found: ${kv._2}")
      })))

}
