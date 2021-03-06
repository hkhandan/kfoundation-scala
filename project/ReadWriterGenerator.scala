// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

import java.io.{File, FileWriter}

import sbt.io.RichFile
import sbt.io.IO


object ReadWriterGenerator {

  private val N = 10

  private val STANDARD_READERS = """
    |def readerOf[T](implicit reader: ValueReader[T]): ValueReader[T] = reader
    |
    |def readerOf(typeName: UString): ValueReader[Unit] = d => {
    |  d.readObjectBegin(typeName)
    |  d.readObjectEnd()
    |}
    |
    |class FlexObjectReader(
    |  typeName: UString,
    |  properties: Map[UString, ValueReadWriter[Any]])
    |  extends ValueReader[Map[UString, Any]]
    |{
    |  override def read(deserializer: ObjectDeserializer): Map[UString, Any] = {
    |    val result = new scala.collection.mutable.HashMap[UString, Any]()
    |
    |    @scala.annotation.tailrec
    |    def loop(): Unit = {
    |      val pToken = deserializer.tryReadPropertyName()
    |      if(pToken.isDefined) {
    |        val pName = pToken.get
    |        val reader = properties.getOrElse(pName, throw new DeserializationError(
    |          "Reader for property is not provided: " + typeName + "." + pName))
    |        result.put(pName, reader.read(deserializer))
    |        loop()
    |      }
    |    }
    |
    |    deserializer.readObjectBegin(typeName)
    |    loop()
    |    deserializer.readObjectEnd()
    |
    |    result.toMap
    |  }
    |}
    |
    |""".stripMargin

  private val STANDARD_WRITERS = """
    |def writerOf[T](implicit writer: ValueWriter[T]): ValueWriter[T] = writer
    |
    |def writerOf(typeName: UString): ValueWriter[Unit] = (d, _) =>
    |  d.writeObjectBegin(typeName)
    |    .writeObjectEnd()
    |
    |class FlexObjectWriter(
    |  typeName: UString,
    |  properties: Map[UString, ValueReadWriter[Any]])
    |  extends ValueWriter[Map[UString, Any]]
    |{
    |  override def write(serializer: ObjectSerializer, value: Map[UString, Any]): Unit = {
    |    serializer.writeObjectBegin(typeName)
    |    value.foreach(kv => {
    |      serializer.writePropertyName(kv._1)
    |      properties.get(kv._1)
    |        .getOrElse(throw new SerializationError("No writer provided for property \"" + kv._1 + "\" of" + typeName))
    |        .write(serializer, kv._2)
    |    })
    |    serializer.writeObjectEnd()
    |  }
    |}
    |
    |""".stripMargin

  private val STANDARD_READ_WRITERS = """
    |implicit val NOTHING: ValueReadWriter[Unit] = new ValueReadWriter[Unit] {
    |  override def write(serializer: ObjectSerializer, value: Unit): Unit = {}
    |  override def read(deserializer: ObjectDeserializer): Unit = {}
    |}
    |
    |implicit val INT: ValueReadWriter[Int] = new ValueReadWriter[Int] {
    |  override def write(serializer: ObjectSerializer, value: Int): Unit =
    |    serializer.writeLiteral(value)
    |  override def read(deserializer: ObjectDeserializer): Int =
    |    deserializer.readIntegerLiteral().toInt
    |}
    |
    |implicit val LONG: ValueReadWriter[Long] = new ValueReadWriter[Long] {
    |  override def write(serializer: ObjectSerializer, value: Long): Unit =
    |    serializer.writeLiteral(value)
    |  override def read(deserializer: ObjectDeserializer): Long =
    |    deserializer.readIntegerLiteral()
    |}
    |
    |implicit val STRING: ValueReadWriter[UString] = new ValueReadWriter[UString] {
    |  override def write(serializer: ObjectSerializer, value: UString): Unit =
    |    serializer.writeLiteral(value)
    |  override def read(deserializer: ObjectDeserializer): UString =
    |    deserializer.readStringLiteral()
    |}
    |
    |implicit val FLOAT: ValueReadWriter[Float] = new ValueReadWriter[Float] {
    |  override def write(serializer: ObjectSerializer, value: Float): Unit =
    |    serializer.writeLiteral(value)
    |  override def read(deserializer: ObjectDeserializer): Float =
    |    deserializer.readDecimalLiteral().toFloat
    |}
    |
    |implicit val DOUBLE: ValueReadWriter[Double] = new ValueReadWriter[Double] {
    |  override def write(serializer: ObjectSerializer, value: Double): Unit =
    |    serializer.writeLiteral(value)
    |  override def read(deserializer: ObjectDeserializer): Double =
    |    deserializer.readDecimalLiteral()
    |}
    |
    |implicit val BOOLEAN: ValueReadWriter[Boolean] = new ValueReadWriter[Boolean] {
    |  override def write(serializer: ObjectSerializer, value: Boolean): Unit =
    |    serializer.writeLiteral(value)
    |  override def read(deserializer: ObjectDeserializer): Boolean =
    |    deserializer.readBooleanLiteral()
    |}
    |
    |def readWriterOf[T](implicit rw: ValueReadWriter[T]): ValueReadWriter[T] = rw
    |
    |def readWriterOf(className: UString): ValueReadWriter[Unit] = new ValueReadWriter[Unit] {
    |  override def write(serializer: ObjectSerializer, value: Unit): Unit =
    |    serializer.writeObjectBegin(className)
    |      .writeObjectEnd()
    |
    |  override def read(deserializer: ObjectDeserializer): Unit = {
    |    deserializer.readObjectBegin(className)
    |    deserializer.readObjectEnd()
    |  }
    |}
    |
    |def readWriterOf[T](reader: ValueReader[T], writer: ValueWriter[T]):
    |ValueReadWriter[T] = new ValueReadWriter[T] {
    |  override def write(serializer: ObjectSerializer, value: T): Unit =
    |    writer.write(serializer, value)
    |  override def read(deserializer: ObjectDeserializer): T =
    |    reader.read(deserializer)
    |}
    |
    |class FlexObjectReadWriter(
    |  typeName: UString,
    |  properties: Map[UString, ValueReadWriter[Any]])
    |  extends ValueReadWriter[Map[UString, Any]]
    |{
    |  private val reader = new ValueReaders.FlexObjectReader(typeName, properties)
    |  private val writer = new ValueWriters.FlexObjectWriter(typeName, properties)
    |
    |  override def write(serializer: ObjectSerializer, value: Map[UString, Any]): Unit =
    |    writer.write(serializer, value)
    |
    |  override def read(deserializer: ObjectDeserializer): Map[UString, Any] =
    |    reader.read(deserializer)
    |}
    |
    |class Tuple1ReadWriter[T1] (
    |  typeName: UString,
    |    field1: (UString, ValueReadWriter[T1]))
    |  extends ValueReadWriter[T1]
    |{
    |  override def write(serializer: ObjectSerializer, value: T1): Unit = {
    |    serializer.writeObjectBegin(typeName)
    |    field1._2.writeProperty(serializer, field1._1, value)
    |    serializer.writeObjectEnd()
    |  }
    |
    |  override def read(deserializer: ObjectDeserializer): T1 = {
    |    deserializer.select(Seq(field1._1))
    |    deserializer.readObjectBegin(typeName)
    |    var v1: Option[T1] = None
    |    var propName = deserializer.tryReadPropertyName()
    |    while(propName.isDefined) {
    |      propName match {
    |        case Some(field1._1) => v1 = Some(field1._2.read(deserializer))
    |        case Some(x) => throw new DeserializationError("Unrecognized field: " + typeName + "." + x)
    |        case _ =>
    |      }
    |      propName = deserializer.tryReadPropertyName()
    |    }
    |    deserializer.readObjectEnd()
    |    v1.getOrElse(field1._2.getDefaultValue)
    |  }
    |}
    |
    |def tuple[T1](typeName: String,
    |  field1: (String, ValueReadWriter[T1])):
    |ValueReadWriter[T1] =
    |  new Tuple1ReadWriter[T1](UString.of(typeName),
    |    UString.of(field1._1) -> field1._2)
    |
    |""".stripMargin

  implicit def toRichFile(file: File): RichFile = new RichFile(file)

  def list(n: Int, del: String, fn: Int => String): String =
    1.to(n).map(fn).mkString(del)

  def typeParam(n: Int): String = list(n, ", ", n => s"T$n")
  def tupleTypeParam(n: Int): String = n.toString + "[" + typeParam(n) + "]"
  def fieldNameList(n: Int): String = list(n, ", ", i => s"fieldName$i: UString")
  def rwMapList(n: Int): String = list(n, ", ", i => s"field$i: (UString, T$i)")
  def className(prefix: String, n: Int): String = prefix + tupleTypeParam(n)
  def readerClassName(n: Int): String = className("ValueReader", n)
  def writerClassName(n: Int): String = className("ValueWriter", n)
  def readWriterClassName(n: Int): String = s"Tuple${n}ReadWriter"

  def readMethod(n: Int, prefix: String): StringBuilder = new StringBuilder()
    .append("  override def read(deserializer: ObjectDeserializer): Tuple").append(tupleTypeParam(n)).append(" = {\n")
    .append("    deserializer.select(Seq(").append(list(n, ", ", i => s"field$i._1")).append("))\n")
    .append(list(n, "\n", i => s"    var v$i: Option[T$i] = None")).append("\n")
    .append("    deserializer.readObject(typeName, _ match {\n")
    .append(list(n, "\n", i => s"      case `fieldName$i` => v$i = Some($prefix$i.read(deserializer))")).append("\n")
    .append("      case _ =>\n")
    .append("    })\n")
    .append("    new Tuple").append(tupleTypeParam(n)).append("(\n")
    .append(list(n, ",\n", i => s"      v$i.getOrElse($prefix$i.getDefaultValue)")).append(")\n")
    .append("  }\n")

  def writeMethod(n: Int, prefix: String): StringBuilder =  new StringBuilder()
    .append("  override def write(serializer: ObjectSerializer, value: Tuple").append(tupleTypeParam(n)).append("): Unit = \n")
    .append("    serializer.writeObjectBegin(typeName)\n")
    .append(list(n, "\n", i => s"      .writePropertyName(fieldName$i).writeValue(value._$i)($prefix$i)")).append("\n")
    .append("      .writeObjectEnd()\n")

  def readerClass(n: Int): String = new StringBuilder()
    .append("private class ").append(readerClassName(n)).append("(\n")
    .append("  typeName: UString, ").append(fieldNameList(n)).append(")(\n")
    .append("  implicit ").append(list(n, ", ", i => s"reader$i: ValueReader[T$i]")).append(")\n")
    .append("  extends ValueReader[Tuple").append(tupleTypeParam(n)).append("]\n")
    .append("{\n")
    .append(readMethod(n, "reader"))
    .append("}\n\n")
    .toString()

  def writerClass(n: Int): String = new StringBuilder()
    .append("private class ").append(writerClassName(n)).append("(\n")
    .append("  typeName: UString, ").append(fieldNameList(n)).append(")(\n")
    .append("  implicit ").append(list(n, ", ", i => s"writer$i: ValueWriter[T$i]")).append(")\n")
    .append("  extends ValueWriter[Tuple").append(tupleTypeParam(n)).append("]\n")
    .append("{\n")
    .append(writeMethod(n, "writer"))
    .append("}\n\n")
    .toString()

  def readWriterClass(n: Int): String = new StringBuilder()
    .append("private class ").append(readWriterClassName(n)).append("(\n")
    .append("  typeName: UString, ").append(fieldNameList(n)).append(")(\n")
    .append("  implicit ").append(list(n, ", ", i => s"rw$i: ValueReadWriter[T$i]")).append(")\n")
    .append("  extends ValueReadWriter[Tuple").append(tupleTypeParam(n)).append("]\n")
    .append("{\n")
    .append(writeMethod(n, "rw")).append('\n')
    .append(readMethod(n, "rw"))
    .append("}\n\n")
    .toString()


  def _readWriterClass(n: Int): String = new StringBuilder()
    .append("class ").append(readWriterClassName(n)).append('[').append(typeParam(n)).append("] (\n")
    .append("  typeName: UString,\n")
    .append("  ").append(list(n, ", ", i => s"  field$i: (UString, ValueReadWriter[T$i])")).append(")\n")
    .append("  extends ValueReadWriter[(").append(typeParam(n)).append(")]\n")
    .append("{\n")
    .append(_writeMethod(n)).append('\n')
    .append(_readMethod(n))
    .append("}\n\n")
    .toString()


  def _readMethod(n: Int): String = new StringBuilder()
    .append("  override def read(deserializer: ObjectDeserializer): (").append(typeParam(n)).append(") = {\n")
    .append("    deserializer.select(Seq(").append(list(n, ", ", i => s"field$i._1")).append("))\n")
    .append("    deserializer.readObjectBegin(typeName)\n")
    .append(list(n, "\n", i => s"    var v$i: Option[T$i] = None")).append("\n")
    .append("    var propName = deserializer.tryReadPropertyName()\n")
    .append("    while(propName.isDefined) {\n")
    .append("      propName match {\n")
    .append(list(n, "", i => s"        case Some(field$i._1) => v$i = Some(field$i._2.read(deserializer))\n"))
    .append("        case Some(x) => throw new DeserializationError(\"Unrecognized field: \" + typeName + \".\" + x)\n")
    .append("        case _ => \n")
    .append("      }\n")
    .append("      propName = deserializer.tryReadPropertyName()\n")
    .append("    }\n")
    .append("    deserializer.readObjectEnd()\n")
    .append("    (").append(list(n, ",\n    ", i => s"v$i.getOrElse(field$i._2.getDefaultValue)")).append(")\n")
    .append("  }\n")
    .toString()


  def _writeMethod(n: Int): String = new StringBuilder()
    .append("  override def write(serializer: ObjectSerializer, value: (").append(typeParam(n)).append(")): Unit = {\n")
    .append("    serializer.writeObjectBegin(typeName)\n")
    .append(list(n, "\n", i => s"    field$i._2.writeProperty(serializer, field$i._1, value._$i)")).append("\n")
    .append("    serializer.writeObjectEnd()\n")
    .append("  }\n")
    .toString()


  def _factory(n: Int): String = new StringBuilder()
    .append("  def tuple[").append(typeParam(n)).append("](typeName: String,\n")
    .append(list(n, ",\n", i => s"    field$i: (String, ValueReadWriter[T$i])")).append("):\n")
    .append("  ValueReadWriter[(").append(typeParam(n)).append(")] = \n")
    .append("    new ").append(readWriterClassName(n)).append("[").append(typeParam(n)).append("](UString.of(typeName),\n")
    .append(list(n, ",\n", i => s"      UString.of(field$i._1) -> field$i._2")).append(")\n\n")
    .toString()


  def factory(prefix: String, typeName: String, n: Int): String = new StringBuilder()
    .append("def ").append(prefix).append("Of[").append(typeParam(n)).append("](\n")
    .append("  typeName: UString, ").append(fieldNameList(n)).append(")(\n")
    .append("  implicit ").append(list(n, ", ", i => s"$prefix$i: $typeName[T$i]")).append(")\n")
    .append(": ").append(typeName).append("[Tuple").append(tupleTypeParam(n)).append("] = \n")
    .append("  new ").append(className(typeName, n)).append("(typeName, ").append(list(n, ", ", i => "fieldName" + i))
    .append(")(").append(list(n, ", ", i => prefix + i)).append(")\n\n")
    .toString()

  def writerFactory(n: Int): String = factory("writer", "ValueWriter", n)
  def readerFactory(n: Int): String = factory("reader", "ValueReader", n)
  def readWriterFactory(n: Int): String = factory("readWriter", "ValueReadWriter", n)
  def nothing(n: Int): String = ""

  def generateFile(dir: File, className: String, staticCode: String, classGen: Int => String,
    factoryGen: Int => String): File =
  {
    val outputFile = dir / s"$className.scala"
    IO.createDirectory(dir)
    val writer = new FileWriter(outputFile)
    writer.write("package net.kfoundation.scala.serialization\n")
    writer.write("import net.kfoundation.scala.UString\n")
    writer.write(s"object $className {\n")
    writer.write(staticCode)
    writer.write("\n")
    2.to(N).foreach(i => writer.write(classGen(i)))
    2.to(N).foreach(i => writer.write(factoryGen(i)))
    writer.write("}")
    writer.close()
    println("Generated: " + outputFile)
    outputFile
  }

  def generateReaders(dir: File): File = generateFile(dir, "ValueReaders",
    STANDARD_READERS, readerClass, readerFactory)

  def generateWriters(dir: File): File = generateFile(dir, "ValueWriters",
    STANDARD_WRITERS, writerClass, writerFactory)

  def generateReadWriters(dir: File): File = generateFile(dir, "ValueReadWriters",
    STANDARD_READ_WRITERS, readWriterClass, readWriterFactory)

  def _generateReaders(dir: File): File = generateFile(dir, "ValueReaders",
    STANDARD_READERS, nothing, nothing)

  def _generateWriters(dir: File): File = generateFile(dir, "ValueWriters",
    STANDARD_WRITERS, nothing, nothing)

  def _generateReadWriters(dir: File): File = generateFile(dir, "ValueReadWriters",
    STANDARD_READ_WRITERS, _readWriterClass, _factory)

}
