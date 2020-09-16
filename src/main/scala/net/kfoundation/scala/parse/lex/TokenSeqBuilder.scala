// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse.lex

import net.kfoundation.scala.parse.syntax.TokenWalker
import net.kfoundation.scala.parse.{CodeRange, MutableCodeLocation}
import net.kfoundation.scala.{UChar, UString}

import scala.collection.mutable



object TokenSeqBuilder {
  private val FILE_NAME = "TokenSeqBuilder"
  def apply() = new TokenSeqBuilder()
}



/**
 * This class is used for testing an interpreter's internals by creating a mock
 * sequence of tokens to be used as input.
 */
class TokenSeqBuilder {
  import TokenSeqBuilder._

  private val loc = new MutableCodeLocation("mock")
  private val tokens = new mutable.ListBuffer[Token[_]]()


  private def step(str: UString): CodeRange =
    step(str.getLength, str.getUtf8Length)


  private def step(cols: Int, bytes: Int): CodeRange = {
    val begin = loc.immutableCopy
    loc.step(cols, bytes)
    new CodeRange(begin, loc.immutableCopy)
  }


  private def add(token: Token[_]): TokenSeqBuilder = {
    tokens.append(token)
    this
  }


  /** Appends an identifier token */
  def id(str: UString): TokenSeqBuilder =
    add(new IdentifierToken(step(str), str))


  /** Appends a punctuation token */
  def p(ch: UChar): TokenSeqBuilder =
    add(new PunctuationToken(step(1, ch.getUtf8Length), ch))


  /** Appends a keyword token */
  def kw(str: UString): TokenSeqBuilder =
    add(new KeywordToken(step(str), str))


  /** Appends an operator token */
  def op(str: UString): TokenSeqBuilder =
    add(new OperatorToken(step(str), str))


  /** Appends an integral token */
  def n(i: Long): TokenSeqBuilder =
    add(new IntegralToken(step(i.toString), i))


  /** Appends a fractional decimal token */
  def n(d: Double): TokenSeqBuilder =
    add(new DecimalToken(step(d.toString), d))


  /** Returns the sequence of tokens built using this object. */
  def getTokens: Seq[Token[_]] = tokens.toSeq


  /** Returns a TokenWalker with this builder's sequence of tokens as input. */
  def buildWalker: TokenWalker = new TokenWalker(FILE_NAME, getTokens)

}