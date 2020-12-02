// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse.lex


/**
 * Token readers are convenient to use in implementing a tokenizer. A
 * collection of TokenReader can be used to tokenize the entire input text.
 */
trait TokenReader[T <: Token[_]] {
  def tryRead(w: CodeWalker): Option[T]
}
