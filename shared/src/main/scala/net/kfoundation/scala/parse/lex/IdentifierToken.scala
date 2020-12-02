// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse.lex

import net.kfoundation.scala.UString
import net.kfoundation.scala.parse.CodeRange



object IdentifierToken {

  /** Attempts to read an identifier from the input source */
  object reader extends TokenReader[IdentifierToken] {
    def tryRead(w: CodeWalker): Option[IdentifierToken] =
      if(w.tryRead(ch => Character.isJavaIdentifierStart(ch)) >= 0) {
        w.readAll(ch => Character.isJavaIdentifierPart(ch))
        val str = w.getCurrentSelection
        val selection = w.commit()
        Some(new IdentifierToken(selection, str))
      } else {
        None
      }
  }

}


/** A portion of input text representing a Java identifier */
class IdentifierToken(range: CodeRange, value: UString)
  extends Token[UString](range, value)
{
  override def equals(obj: Any): Boolean = obj match {
    case a: IdentifierToken =>
      super.equals(a) &&
      a.value.equals(value)
    case _ => false
  }
}
