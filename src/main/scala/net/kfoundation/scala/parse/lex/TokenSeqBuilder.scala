package net.kfoundation.scala.parse.lex

import net.kfoundation.scala.parse.syntax.TokenWalker
import net.kfoundation.scala.parse.{CodeRange, MutableCodeLocation}
import net.kfoundation.scala.{UChar, UString}

import scala.collection.mutable


object TokenSeqBuilder {
  private val FILE_NAME = "TokenSeqBuilder"
  def apply() = new TokenSeqBuilder()
}


class TokenSeqBuilder {
  import TokenSeqBuilder._

  private val loc = new MutableCodeLocation()
  private val tokens = new mutable.ListBuffer[Token[_]]()

  private def step(str: UString): CodeRange =
    step(str.getLength, str.getUtf8Length)

  private def step(cols: Int, bytes: Int): CodeRange = {
    val begin = loc.immutableCopy
    loc.step(cols, bytes)
    new CodeRange(FILE_NAME, begin, loc.immutableCopy)
  }

  private def add(token: Token[_]): TokenSeqBuilder = {
    tokens.append(token)
    this
  }

  def id(str: UString): TokenSeqBuilder =
    add(new IdentifierToken(step(str), str))

  def p(ch: UChar): TokenSeqBuilder =
    add(new PunctuationToken(step(1, ch.getUtf8Length), ch))

  def kw(str: UString): TokenSeqBuilder =
    add(new KeywordToken(step(str), str))

  def op(str: UString): TokenSeqBuilder =
    add(new OperatorToken(step(str), str))

  def n(i: Long): TokenSeqBuilder =
    add(new IntegralToken(step(i.toString), i))

  def n(d: Double): TokenSeqBuilder =
    add(new DecimalToken(step(d.toString), d))

  def getTokens: Seq[Token[_]] = tokens.toSeq
  def buildWalker: TokenWalker = new TokenWalker(FILE_NAME, getTokens)
}