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



/** Common interface for serializer factories */
trait ObjectSerializerFactory {
  def of(output: OutputStream, indentSize: Int, compact: Boolean): ObjectSerializer
  def of(output: OutputStream): ObjectSerializer
  def of(output: OutputStream, indentSize: Int): ObjectSerializer =
    of(output, indentSize, false)
}