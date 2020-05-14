package net.kfoundation.lang.lex

import net.kfoundation.UString
import net.kfoundation.lang.CodeRange

object IdentifierToken {
  object reader extends TokenReader[IdentifierToken] {
    def tryRead(w: CodeWalker): Option[IdentifierToken] =
      if(w.tryRead(ch => Character.isJavaIdentifierStart(ch)) >= 0) {
        w.tryReadAll(ch => Character.isJavaIdentifierPart(ch))
        val str = w.getCurrentSelection
        val selection = w.commit()
        Some(new IdentifierToken(selection, str))
      } else {
        None
      }
  }
}

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
