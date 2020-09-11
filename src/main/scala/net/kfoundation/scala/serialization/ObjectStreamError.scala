package net.kfoundation.scala.serialization

import java.io.IOException

class ObjectStreamError(message: String, cause: Throwable)
  extends IOException(message, cause)
{
  def this(message: String) = this(message, null)
}