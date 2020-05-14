package net.kfoundation.lang.lex

import net.kfoundation.lang.CodeRange
import net.kfoundation.{UChar, UString}

import scala.collection.{immutable, mutable}

object KeywordToken {
  abstract class Reader extends TokenReader[KeywordToken] {
    private val values = new mutable.ListBuffer[UString]

    def getValues: immutable.Seq[UString] = values.toStream

    protected def add(newValue: UString): UString = {
      values.append(newValue)
      newValue
    }

    override def tryRead(w: CodeWalker): Option[KeywordToken] =
      if(w.tryReadAll(ch => UChar.isAlphabet(ch)) <= 0) {
        None
      } else {
        val str = w.getCurrentSelection
        values.find(_.equals(str))
          .map(str => new KeywordToken(w.commit(), str))
          .orElse({
            w.rollback()
            None
          })
      }

    def convert(id: IdentifierToken): Option[KeywordToken] =
      values.find(v => id.value.equals(v))
        .map(v => new KeywordToken(id, v))
  }
}

class KeywordToken(range: CodeRange, value: UString)
  extends Token[UString](range, value)
{
  override def canEqual(other: Any): Boolean = other.isInstanceOf[KeywordToken]

  override def equals(other: Any): Boolean = other match {
    case that: KeywordToken =>
      super.equals(that) &&
        (that canEqual this) &&
        value == that.value
    case _ => false
  }
}