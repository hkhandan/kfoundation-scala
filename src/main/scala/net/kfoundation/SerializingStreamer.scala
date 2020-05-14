package net.kfoundation

import java.io.{ByteArrayOutputStream, OutputStream}

trait SerializingStreamer {
  def serialize(serializer: ObjectSerializer): Unit

  def writeToK4Stream(os: OutputStream): Unit =
    serialize(new K4Serializer(os))

  def toUString: UString = {
    val os = new ByteArrayOutputStream()
    writeToK4Stream(os)
    UString.of(os.toByteArray)
  }

  override def toString: String = toUString.toString
}
