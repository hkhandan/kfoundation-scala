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
    |private class ValueReader1[T1](
    |  typeName: UString, fieldName1: UString)(
    |  implicit reader1: ValueReader[T1])
    |  extends ValueReader[T1]
    |{
    |  override def read(deserializer: ObjectDeserializer): T1 = {
    |    var v1: Option[T1] = None
    |    deserializer.readObject(typeName, _ match {
    |      case `fieldName1` => v1 = Some(reader1.read(deserializer))
    |    })
    |    v1.getOrElse(reader1.getDefaultValue)
    |  }
    |}
    |
    |def readerOf[T1](
    |  typeName: UString, fieldName1: UString)(
    |  implicit reader1: ValueReader[T1])
    |: ValueReader[T1] =
    |  new ValueReader1[T1](typeName, fieldName1)(reader1)
    |""".stripMargin

  private val STANDARD_READ_WRITERS = """
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
    |private class ValueReadWriter1[T1](
    |  typeName: UString, fieldName1: UString)(
    |  implicit rw1: ValueReadWriter[T1])
    |  extends ValueReadWriter[T1]
    |{
    |  override def write(serializer: ObjectSerializer, value: T1): Unit =
    |    serializer.writeObjectBegin(typeName)
    |      .writePropertyName(fieldName1).writeValue(value)(rw1)
    |      .writeObjectEnd()
    |
    |  override def read(deserializer: ObjectDeserializer): T1 = {
    |    var v1: Option[T1] = None
    |    deserializer.readObject(typeName, _ match {
    |      case `fieldName1` => v1 = Some(rw1.read(deserializer))
    |      case _ =>
    |    })
    |    v1.getOrElse(rw1.getDefaultValue)
    |  }
    |}
    |
    |def readWriterOf[T1](
    |  typeName: UString, fieldName1: UString)(
    |  implicit readWriter1: ValueReadWriter[T1])
    |: ValueReadWriter[T1] =
    |  new ValueReadWriter1[T1](typeName, fieldName1)(readWriter1)
    |""".stripMargin

  private val STANDARD_WRITERS = """
    |def writerOf[T](implicit writer: ValueWriter[T]): ValueWriter[T] = writer
    |
    |def writerOf(typeName: UString): ValueWriter[Unit] = (d, _) =>
    |  d.writeObjectBegin(typeName)
    |    .writeObjectEnd()
    |
    |private class ValueWriter1[T1](
    |  typeName: UString, fieldName1: UString)(
    |  implicit writer1: ValueWriter[T1])
    |  extends ValueWriter[T1]
    |{
    |  override def write(serializer: ObjectSerializer, value: T1): Unit =
    |    serializer.writeObjectBegin(typeName)
    |      .writePropertyName(fieldName1).writeValue(value)(writer1)
    |      .writeObjectEnd()
    |}
    |
    |def writerOf[T1](
    |  typeName: UString, fieldName1: UString)(
    |  implicit writer1: ValueWriter[T1])
    |: ValueWriter[T1] =
    |  new ValueWriter1[T1](typeName, fieldName1)(writer1)
    |
    |""".stripMargin

  implicit def toRichFile(file: File): RichFile = new RichFile(file)

  def list(n: Int, del: String, fn: Int => String): String =
    1.to(n).map(fn).mkString(del)

  def typeParam(n: Int): String = list(n, ", ", n => s"T$n")
  def tupleTypeParam(n: Int): String = n.toString + "[" + typeParam(n) + "]"
  def fieldNameList(n: Int): String = list(n, ", ", i => s"fieldName$i: UString")
  def className(prefix: String, n: Int): String = prefix + tupleTypeParam(n)
  def readerClassName(n: Int): String = className("ValueReader", n)
  def writerClassName(n: Int): String = className("ValueWriter", n)
  def readWriterClassName(n: Int): String = className("ValueReadWriter", n)

  def readMethod(n: Int, prefix: String): StringBuilder = new StringBuilder()
    .append("  override def read(deserializer: ObjectDeserializer): Tuple").append(tupleTypeParam(n)).append(" = {\n")
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
    .append(writeMethod(n, "rw")).append("\n")
    .append(readMethod(n, "rw"))
    .append("}\n\n")
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

}
