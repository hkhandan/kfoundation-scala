// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.db

import net.kfoundation.scala.UString
import net.kfoundation.scala.serialization.internals.IndentingWriter
import net.kfoundation.scala.serialization.{ObjectSerializer, ObjectSerializerFactory}

import java.io.OutputStream


object UpdateQueryObjectSerializer {
  val MIME_TYPE: UString = "application/sql"

  val FACTORY: ObjectSerializerFactory = new ObjectSerializerFactory {
    override def of(output: OutputStream, indentSize: Int, compact: Boolean): ObjectSerializer =
      new UpdateQueryObjectSerializer(new IndentingWriter(output, indentSize, compact))

    override def getMediaType: UString = MIME_TYPE
  }
}


class UpdateQueryObjectSerializer(writer: IndentingWriter)
  extends AbstractQueryObjectSerializer
{
  private def writeField(t: (UString, UString)): Unit = {
    writer.writeNewLine()
    writer.write(t._1)
    writer.write("=")
    writer.write(t._2)
  }

  override protected def write(
    table: UString, fields: Seq[(UString, UString)]): Unit =
  {
    writer.write("update \"")
    writer.write(table)
    writer.write("\" set")
    writer.indent()
    fields.headOption.foreach(writeField)
    fields.tail.foreach(t => {
      writer.write(",")
      writeField(t)
    })
  }
}