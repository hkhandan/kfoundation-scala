package net.kfoundation.lang.lex

import net.kfoundation.UChar
import net.kfoundation.lang.CodeRange
import scala.collection.{immutable, mutable}


object PunctuationToken {
  class Reader extends TokenReader[PunctuationToken] {
    private val values = new mutable.ListBuffer[UChar]()

    def getValues: immutable.Seq[UChar] = values.toStream

    def add(ch: UChar): UChar = {
      values.append(ch)
      ch
    }

    override def tryRead(w: CodeWalker): Option[PunctuationToken] =
      values.find(w.tryRead)
        .map(ch => new PunctuationToken(w.commit(), ch))
  }
}


class PunctuationToken(range: CodeRange, value: UChar)
  extends Token[UChar](range, value)