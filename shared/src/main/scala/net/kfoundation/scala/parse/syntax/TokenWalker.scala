// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse.syntax

import net.kfoundation.scala.{UChar, UString}
import net.kfoundation.scala.parse.CodeRange
import net.kfoundation.scala.parse.lex._



/**
 * Facility to process the output of tokenizer mainly for the purpose of
 * building an abstract syntax tree (AST).
 */
class TokenWalker(file: String, tokens: Seq[Token[_]]) {
  private var cursor: Int = 0


  /** Move to next token */
  def step(): Unit = cursor += 1


  /** Get index of current token processes */
  def getCursor: Int = cursor


  /** Returns the current token */
  def thisToken: Option[Token[_]] = tokens.lift(cursor)


  /** Returns the token immediately before the current one */
  def lastToken: Option[Token[_]] = tokens.lift(cursor-1)


  /**
   * Computes the code range starting at the beginning of the token at the
   * given index to the end of the current token.
   */
  def range(begin: Int): CodeRange =
    new CodeRange(tokens(begin).begin, tokens(cursor-1).end)


  /**
   * Facility to produce a syntax error containing useful information about
   * code location, and tokens surrounding where error has occurred.
   */
  def syntaxError(expectation: UString): SyntaxError =
    SyntaxError.expected(file, lastToken, thisToken, expectation)


  /**
   * Test if the current token is a keyword.
   */
  def isKeyword(text: UString): Boolean = thisToken match {
    case Some(t: KeywordToken) => t.value.equals(text)
    case _ => false
  }


  /**
   * Tests if current token is a punctuation.
   */
  def isPunctuation(symbol: UChar): Boolean = thisToken match {
    case Some(t: PunctuationToken) => t.value.equals(symbol)
    case _ => false
  }


  /**
   * Moves to the token if the current one is a keyword, otherwise
   * throws a syntax error.
   *
   * @throws SyntaxError if the current token is not the desired keyword.
   */
  def expectKeyword(text: UString): Unit =
    if(isKeyword(text))
      step()
    else
      throw syntaxError(text)


  /**
   * Moves to the next token if the current one is a punctuation, otherwise
   * throws a syntax error.
   *
   * @throws SyntaxError when current token is not the desired symbol.
   */
  def expectPunctuation(symbol: UChar): Unit =
    if(isPunctuation(symbol))
      step()
    else
      throw syntaxError(symbol.toString)


  /**
   * Moves to the next token if the current one is subclass of the given class,
   * otherwise throws a syntax error.
   *
   * @return the current token before stepping forward
   * @throws SyntaxError when current token is not of the given type.
   */
  def expect[T <: Token[_]](cls: Class[T]): T = thisToken match {
    case Some(t) if t.getClass.equals(cls) =>  step(); t.asInstanceOf[T]
    case _ => throw syntaxError(cls.getSimpleName)
  }


  /**
   * Moves to the next token if the current one is an identifier, returning
   * this token, otherwise throws a syntax error.
   *
   * @return current token
   * @throws SyntaxError if current token is not an identifier.
   */
  def expectIdentifier(): IdentifierToken =
    expect(classOf[IdentifierToken])

}
