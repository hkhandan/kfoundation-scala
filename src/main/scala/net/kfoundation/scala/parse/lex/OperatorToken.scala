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



object OperatorToken {

  /**
   * Attempts to read an operator from input. Determinatino of what constitutes
   * an operator is up to the implementing class.
   */
  abstract class Reader extends TokenReader[OperatorToken] {
    private val values = new mutable.ListBuffer[UString]

    def getValues: Seq[UString] = values.toSeq

    protected def add(newValue: UString): UString = {
      values.append(newValue)
      newValue
    }

    override def tryRead(w: CodeWalker): Option[OperatorToken] =
      values.find(w.tryRead)
        .map(s => new OperatorToken(w.commit(), s))
  }
}


/**
 * A portion of input text that is an operator
 */
class OperatorToken(range: CodeRange, value: UString)
  extends Token[UString](range, value)