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

import java.io.{InputStream, OutputStream}


object ObjectBiFactory {
  class Adapter(
      serializer: ObjectSerializerFactory,
      deserializer: ObjectDeserializerFactory)
    extends ObjectBiFactory
  {
    override def of(output: OutputStream, indentSize: Int, compact: Boolean):
    ObjectSerializer = serializer.of(output, indentSize, compact)

    override def getMediaType: UString = serializer.getMediaType

    override def of(input: InputStream): ObjectDeserializer =
      deserializer.of(input)
  }
}


/**
 * An object bi-factory can produces serializers as well as deserializers for
 * a given target format.
 */
trait ObjectBiFactory extends ObjectSerializerFactory
  with ObjectDeserializerFactory