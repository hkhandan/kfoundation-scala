package net.kfoundation.lang.lex

import net.kfoundation.UString
import net.kfoundation.lang.CodeRange
import scala.collection.{immutable, mutable}


object OperatorToken {
  abstract class Reader extends TokenReader[OperatorToken] {
    private val values = new mutable.ListBuffer[UString]

    def getValues: immutable.Seq[UString] = values.toStream

    protected def add(newValue: UString): UString = {
      values.append(newValue)
      newValue
    }

    override def tryRead(w: CodeWalker): Option[OperatorToken] =
      values.find(w.tryRead)
        .map(s => new OperatorToken(w.commit(), s))
  }
}


class OperatorToken(range: CodeRange, value: UString)
  extends Token[UString](range, value)