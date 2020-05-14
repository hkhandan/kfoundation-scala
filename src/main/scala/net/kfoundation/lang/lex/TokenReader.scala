package net.kfoundation.lang.lex

trait TokenReader[T <: Token[_]] {
  def tryRead(w: CodeWalker): Option[T]
}
