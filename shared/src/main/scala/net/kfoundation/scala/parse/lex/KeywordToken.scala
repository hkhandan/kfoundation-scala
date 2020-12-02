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

import scala.collection.{mutable, Seq}


object KeywordToken {

  /**
   * Attempts to read a keyword from the input source. Determination of what
   * constitutes a keyword is up to the subclass, as long as it is a sequence
   * of alphabetical letters.
   */
  abstract class Reader extends TokenReader[KeywordToken] {
    private val values = new mutable.ListBuffer[UString]

    def getValues: Seq[UString] = values.toSeq

    protected def add(newValue: UString): UString = {
      values.append(newValue)
      newValue
    }

    override def tryRead(w: CodeWalker): Option[KeywordToken] =
      values.find(v => w.tryRead(v))
        .map(v => new KeywordToken(w.commit(), v))

    def convert(id: IdentifierToken): Option[KeywordToken] =
      values.find(v => id.value.equals(v))
        .map(v => new KeywordToken(id, v))
  }

}



/**
 * A portion of input text considered as a keyword.
 */
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