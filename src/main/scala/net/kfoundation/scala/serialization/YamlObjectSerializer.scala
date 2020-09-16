package net.kfoundation.scala.serialization

import java.io.OutputStream

import net.kfoundation.scala.UString
import net.kfoundation.scala.serialization.internals.{IndentingWriter, ObjectStreamStateMachine}



object YamlObjectSerializer {
  val DEFAULT_INDENT_SIZE = 2

  val FACTORY: ObjectSerializerFactory = new ObjectSerializerFactory {
    override def of(output: OutputStream, indentSize: Int, compact: Boolean):
    ObjectSerializer =
      new YamlObjectSerializer(new IndentingWriter(output, indentSize, compact))

    override def of(output: OutputStream): ObjectSerializer =
      of(output, DEFAULT_INDENT_SIZE, false)
  }
}


/** YAML object serializer */
class YamlObjectSerializer private(writer: IndentingWriter) extends ObjectSerializer {
  import ObjectStreamStateMachine.State
  import internals.CommonSymbols._


  private val stateMachine = new ObjectStreamStateMachine
  private var isInCollection: Boolean = false


  override def writePropertyName(name: UString): ObjectSerializer = {
    writer.writeNewLine()

    if(isInCollection) {
      if(stateMachine.isFirst) {
        writer.write(DASH, SPACE)
      } else {
        writer.write(SPACE, SPACE)
      }
    }

    stateMachine.property()

    writer.write(name)
    writer.write(COLON)

    this
  }


  private def literal(): Unit = {
    stateMachine.literal()
    writer.write(SPACE)
  }


  override def writeLiteral(value: UString): ObjectSerializer = {
    literal()
    writer.write(DOUBLE_QUOTE)
    writer.write(value)
    writer.write(DOUBLE_QUOTE)
    this
  }


  override def writeLiteral(value: Long): ObjectSerializer = {
    literal()
    writer.write(UString.of(value))
    this
  }


  override def writeLiteral(value: Double): ObjectSerializer = {
    literal()
    writer.write(UString.of(value))
    this
  }


  override def writeLiteral(value: Boolean): ObjectSerializer = {
    literal()
    writer.write(booleanToString(value))
    this
  }


  override def writeNull(): ObjectSerializer = {
    literal()
    writer.write(NULL)
    this
  }


  override def writeObjectBegin(name: UString): ObjectSerializer = {
    isInCollection = stateMachine.isInCollection

    if(stateMachine.getState == State.STREAM_BEGIN) {
      writer.write(name)
      writer.write(COLON)
    }

    stateMachine.objectBegin(name)

    writer.indent()
    this
  }


  override def writeObjectEnd(): ObjectSerializer = {
    stateMachine.objectEnd()
    isInCollection = false
    writer.unindent()
    this
  }


  override def writeCollectionBegin(): ObjectSerializer = {
    stateMachine.collectionBegin()
    this
  }


  override def writeCollectionEnd(): ObjectSerializer = {
    stateMachine.collectionEnd()
    this
  }


  override def writeStreamEnd(): Unit = stateMachine.streamEnd()
}
