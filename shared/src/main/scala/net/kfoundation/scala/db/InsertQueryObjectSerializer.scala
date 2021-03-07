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


object InsertQueryObjectSerializer {
  val MIME_TYPE: UString = "application/sql"

  val FACTORY: ObjectSerializerFactory = new ObjectSerializerFactory {
    override def of(output: OutputStream, indentSize: Int, compact: Boolean): ObjectSerializer =
      new InsertQueryObjectSerializer(new IndentingWriter(output, indentSize, compact))

    override def getMediaType: UString = MIME_TYPE
  }
}


class InsertQueryObjectSerializer(writer: IndentingWriter)
  extends AbstractQueryObjectSerializer
{

  override protected def write(
    table: UString, fields: Seq[(UString, UString)]): Unit =
  {
    val names = fields.map(_._1)
    val values = fields.map(_._2)

    writer.write("insert into \"")
    writer.write(table)
    writer.write("\"(")
    writer.write(UString.join(names, ", "))
    writer.write(")")
    writer.write(" values(")
    writer.indent()
    writer.writeNewLine()
    writer.write(UString.join(values, ", "))
    writer.write(")")
  }

}