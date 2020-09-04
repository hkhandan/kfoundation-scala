package net.kfoundation.scala.encoding

class DecodingException(message: String, cause: Throwable)
  extends Error(message, cause)
{
  def this(message: String) = this(message, null)
}
