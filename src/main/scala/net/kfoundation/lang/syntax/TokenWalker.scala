package net.kfoundation.lang.syntax

import net.kfoundation.{UChar, UString}
import net.kfoundation.lang.CodeRange
import net.kfoundation.lang.lex._

class TokenWalker(file: String, tokens: Seq[Token[_]]) {
  private var cursor: Int = 0

  def step(): Unit = cursor += 1
  def getCursor: Int = cursor
  def thisToken: Option[Token[_]] = tokens.lift(cursor)
  def lastToken: Option[Token[_]] = tokens.lift(cursor-1)
  def range(begin: Int): CodeRange =
    new CodeRange(file, tokens(begin).begin, tokens(cursor-1).end)

  def syntaxError(expectation: UString): SyntaxError =
    SyntaxError.expected(file, lastToken, thisToken, expectation)

  def isKeyword(text: UString): Boolean = thisToken match {
    case Some(t: KeywordToken) => t.value.equals(text)
    case _ => false
  }

  def isPunctuation(symbol: UChar): Boolean = thisToken match {
    case Some(t: PunctuationToken) => t.value.equals(symbol)
    case _ => false
  }

  def expectKeyword(text: UString): Unit =
    if(isKeyword(text))
      step()
    else
      throw syntaxError(text)

  def expectPunctuation(symbol: UChar): Unit =
    if(isPunctuation(symbol))
      step()
    else
      throw syntaxError(symbol.toString)

  def expect[T <: Token[_]](cls: Class[T]): T = thisToken match {
    case Some(t: T) => step(); t
    case _ => throw syntaxError(cls.getSimpleName)
  }

  def expectIdentifier(): IdentifierToken =
    expect(classOf[IdentifierToken])
}
