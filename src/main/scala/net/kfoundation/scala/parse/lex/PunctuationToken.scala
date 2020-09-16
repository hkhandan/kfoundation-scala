// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse.lex

import net.kfoundation.scala.UChar
import net.kfoundation.scala.parse.CodeRange

import scala.collection.{mutable, Seq}



object PunctuationToken {

  /**
   * Attempts to read a punctuation symbol from input. This class is mutable.
   * Acceptable symbols can be specified using add() method.
   */
  class Reader extends TokenReader[PunctuationToken] {
    private val values = new mutable.ListBuffer[UChar]()

    def getValues: Seq[UChar] = values.toSeq

    def add(ch: UChar): UChar = {
      values.append(ch)
      ch
    }

    override def tryRead(w: CodeWalker): Option[PunctuationToken] =
      values.find(w.tryRead)
        .map(ch => new PunctuationToken(w.commit(), ch))
  }

}



/**
 * A portion of the input text that is a punctuation symbol, such a delimiter.
 */
class PunctuationToken(range: CodeRange, value: UChar)
  extends Token[UChar](range, value)