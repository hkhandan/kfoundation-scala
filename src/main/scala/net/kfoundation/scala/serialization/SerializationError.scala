package net.kfoundation.scala.serialization

class SerializationError(message: String, cause: Throwable)
  extends Exception(message, cause)
{

  def this(message: String) = this(message, null)

}
