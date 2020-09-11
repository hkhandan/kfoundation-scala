package net.kfoundation.scala.serialization

import java.io.OutputStream

import net.kfoundation.scala.UString
import net.kfoundation.scala.serialization.internals.{IndentingWriter, ObjectStreamStateMachine}



object K4ObjectSerializer {
  val DEFAULT_INDENT_SIZE = 2


  val FACTORY: ObjectSerializerFactory = new ObjectSerializerFactory {
    override def of(output: OutputStream, indentSize: Int, compact: Boolean):
    ObjectSerializer =
      new K4ObjectSerializer(new IndentingWriter(output, indentSize, compact))

    override def of(output: OutputStream): ObjectSerializer =
      of(output, DEFAULT_INDENT_SIZE, false)
  }
}



class K4ObjectSerializer private(writer: IndentingWriter)  extends ObjectSerializer {
  import internals.CommonSymbols._
  import ObjectStreamStateMachine.State


  private val stateMachine = new ObjectStreamStateMachine


  override def writePropertyName(name: UString): ObjectSerializer = {
    if(stateMachine.getState == State.OBJECT_END
      || stateMachine.getState == State.COLLECTION_END)
    {
      writer.writeNewLine()
    } else if(!stateMachine.isFirst) {
      writer.write(SPACE)
    }

    stateMachine.property()

    writer.write(name)
    writer.write(EQUAL)
    this
  }


  override def writeLiteral(value: UString): ObjectSerializer = {
    stateMachine.literal()
    writer.write(DOUBLE_QUOTE)
    writer.write(value)
    writer.write(DOUBLE_QUOTE)
    this
  }


  override def writeLiteral(value: Long): ObjectSerializer = {
    stateMachine.literal()
    writer.write(UString.of(value))
    this
  }


  override def writeLiteral(value: Double): ObjectSerializer = {
    stateMachine.literal()
    writer.write(UString.of(value))
    this
  }


  override def writeLiteral(value: Boolean): ObjectSerializer = {
    stateMachine.literal()
    writer.write(booleanToString(value))
    this
  }


  override def writeNull(): ObjectSerializer = {
    stateMachine.literal()
    writer.write(NULL)
    this
  }


  override def writeObjectBegin(name: UString): ObjectSerializer = {
    if(stateMachine.isInCollection) {
      writer.writeNewLine()
    }

    stateMachine.objectBegin(name)

    writer.write(name)
    writer.write(OPEN_BRACE)
    writer.indent()
    this
  }


  override def writeObjectEnd(): ObjectSerializer = {
    stateMachine.objectEnd()
    writer.write(CLOSE_BRACE)
    writer.unindent()
    this
  }


  override def writeCollectionBegin(): ObjectSerializer = {
    stateMachine.collectionBegin()
    writer.write(OPEN_CURLY_BRACE)
    writer.indent()
    this
  }


  override def writeCollectionEnd(): ObjectSerializer = {
    stateMachine.collectionEnd()
    writer.write(CLOSE_CURLY_BRACE)
    writer.unindent()
    this
  }


  override def writeStreamEnd(): Unit = stateMachine.streamEnd()
}
