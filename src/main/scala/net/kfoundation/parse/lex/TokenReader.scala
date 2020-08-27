package net.kfoundation.parse.lex

trait TokenReader[T <: Token[_]] {
  def tryRead(w: CodeWalker): Option[T]
}
