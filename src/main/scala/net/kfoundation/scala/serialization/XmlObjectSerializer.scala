package net.kfoundation.scala.serialization

import java.io.OutputStream

import net.kfoundation.scala.UString
import net.kfoundation.scala.serialization.internals.{IndentingWriter, ObjectStreamStateMachine}



object XmlObjectSerializer {
  val DEFAULT_INDENT_SIZE = 4


  val FACTORY: ObjectSerializerFactory = new ObjectSerializerFactory {
    override def of(output: OutputStream, indentSize: Int, compact: Boolean):
    ObjectSerializer =
      new XmlObjectSerializer(new IndentingWriter(output, indentSize, compact))

    override def of(output: OutputStream): ObjectSerializer =
      of(output, DEFAULT_INDENT_SIZE, false)
  }
}



class XmlObjectSerializer private(writer: IndentingWriter) extends ObjectSerializer {
  import internals.CommonSymbols._
  import internals.XmlSymbols._


  private val stateMachine = new ObjectStreamStateMachine
  private var lastPropertyName: Option[UString] = None


  private def writeOpenTag(name: UString): Unit = {
    writer.write(LT)
    writer.write(name)
    writer.write(GT)
  }


  private def writeCloseTag(name: UString): Unit = {
    writer.write(LT_SLASH)
    writer.write(name)
    writer.write(GT)
  }


  private def writeEmptyTag(name: UString): Unit = {
    writer.write(GT)
    writer.write(name)
    writer.write(SLASH_GT)
  }


  override def writeObjectBegin(name: UString): ObjectSerializer = {
    val tagName = lastPropertyName.getOrElse(name)
    stateMachine.objectBegin(tagName)
    writeOpenTag(tagName)
    lastPropertyName = None
    writer.indent()
    this
  }


  override def writeObjectEnd(): ObjectSerializer = {
    val name = stateMachine.objectEnd().get
    writer.unindent()
    writer.writeNewLine()
    writeCloseTag(name)
    this
  }


  override def writeCollectionBegin(): ObjectSerializer = {
    val tagName = lastPropertyName
      .getOrElse(throw stateMachine.error("collection should follow a property name"))
    lastPropertyName = None

    stateMachine.collectionBegin(tagName)

    writeOpenTag(tagName)
    writer.indent()
    writer.writeNewLine()

    this
  }


  override def writeCollectionEnd(): ObjectSerializer = {
    val tagName = stateMachine.collectionEnd()
      .getOrElse(throw stateMachine.error("collection missing name"))

    writer.writeNewLine()
    writer.unindent()
    writeCloseTag(tagName)

    this
  }


  override def writePropertyName(name: UString): ObjectSerializer = {
    stateMachine.property()
    writer.writeNewLine()
    lastPropertyName = Some(name)
    this
  }


  override def writeLiteral(value: UString): ObjectSerializer = {
    stateMachine.literal()

    val tagName = lastPropertyName.getOrElse(
      throw stateMachine.error("Literal does not follow a property"))

    writeOpenTag(tagName)
    writer.write(value)
    writeCloseTag(tagName)

    lastPropertyName = None
    this
  }


  override def writeLiteral(value: Long): ObjectSerializer =
    writeLiteral(UString.of(value))


  override def writeLiteral(value: Double): ObjectSerializer =
    writeLiteral(UString.of(value))


  override def writeLiteral(value: Boolean): ObjectSerializer =
    writeLiteral(booleanToString(value))


  override def writeNull(): ObjectSerializer = {
    stateMachine.literal()
    writeEmptyTag(lastPropertyName.getOrElse(
      throw stateMachine.error("Literal does not follow a property")))
    writer.unindent()
    lastPropertyName = None
    this
  }


  override def writeStreamEnd(): Unit = stateMachine.streamEnd()
}
