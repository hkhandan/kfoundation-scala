// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization

import java.io.InputStream

import net.kfoundation.scala.UString
import net.kfoundation.scala.io.Path



/** Common interface for deserializer factories */
trait ObjectDeserializerFactory {
  def of(input: InputStream): ObjectDeserializer

  def getMediaType: UString

  def parse[T](str: UString)(implicit reader: ValueReader[T]): T =
    reader.read(this, str)

  def parse[T](path: Path)(implicit reader: ValueReader[T]): T =
    reader.read(this, path)
}
