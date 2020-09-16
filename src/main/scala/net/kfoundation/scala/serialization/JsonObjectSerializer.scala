// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization

import java.io.OutputStream

import net.kfoundation.scala.UString
import net.kfoundation.scala.UString.UStringInterpolation
import net.kfoundation.scala.serialization.internals.{IndentingWriter, ObjectStreamStateMachine}



object JsonObjectSerializer {
  private val COLON_SPACE = U": "
  private val COMMA_SPACE = U", "
  val DEFAULT_INDENT_SIZE = 2


  val FACTORY: ObjectSerializerFactory = new ObjectSerializerFactory {
    override def of(output: OutputStream, indentSize: Int, compact: Boolean): ObjectSerializer =
      new JsonObjectSerializer(new IndentingWriter(output, indentSize, compact))

    override def of(output: OutputStream): ObjectSerializer =
      of(output, DEFAULT_INDENT_SIZE, false)
  }
}


/** JSON object serializer. */
class JsonObjectSerializer private(writer: IndentingWriter) extends ObjectSerializer {
  import internals.CommonSymbols._
  import JsonObjectSerializer._


  private val stateMachine = new ObjectStreamStateMachine


  override def writeObjectBegin(name: UString): ObjectSerializer = {
    if(stateMachine.isInCollection && !stateMachine.isFirst) {
      writer.write(COMMA_SPACE)
    }
    stateMachine.objectBegin(name)
    writer.write(OPEN_CURLY_BRACE)
    writer.indent()
    this
  }


  override def writeObjectEnd(): ObjectSerializer = {
    stateMachine.objectEnd()
    writer.unindent()
    writer.writeNewLine()
    writer.write(CLOSE_CURLY_BRACE)
    this
  }


  override def writeCollectionBegin(): ObjectSerializer = {
    stateMachine.collectionBegin()
    writer.write(OPEN_BRACE)
    writer.indent()
    writer.writeNewLine()
    this
  }


  override def writeCollectionEnd(): ObjectSerializer = {
    stateMachine.collectionEnd()
    writer.writeNewLine()
    writer.unindent()
    writer.write(CLOSE_BRACE)
    this
  }


  override def writePropertyName(name: UString): ObjectSerializer = {
    if(!stateMachine.isFirst) {
      writer.write(COMMA)
    }

    stateMachine.property()

    writer.writeNewLine()
    writer.write(name)
    writer.write(COLON_SPACE)
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


  override def writeStreamEnd(): Unit = {
    stateMachine.streamEnd()
  }
}