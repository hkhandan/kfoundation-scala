package net.kfoundation.scala.parse.lex

trait TokenReader[T <: Token[_]] {
  def tryRead(w: CodeWalker): Option[T]
}
