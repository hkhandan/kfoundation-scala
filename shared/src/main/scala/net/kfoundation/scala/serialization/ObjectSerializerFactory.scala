// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization

import net.kfoundation.scala.UString

import java.io.{ByteArrayOutputStream, OutputStream}



/** Common interface for serializer factories */
trait ObjectSerializerFactory {
  def of(output: OutputStream, indentSize: Int, compact: Boolean): ObjectSerializer

  def getMediaType: UString

  def toString[T](value: T)(implicit writer: ValueWriter[T]): UString = {
    val output = new ByteArrayOutputStream()
    writer.write(this, output, value)
    val result = UString.of(output.toByteArray)
    output.close()
    result
  }
}