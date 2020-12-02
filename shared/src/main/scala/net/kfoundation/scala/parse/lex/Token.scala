// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse.lex

import net.kfoundation.scala.parse.CodeRange



/**
 * Represents a portion of input that constitutes an interpretable lingual unit.
 */
abstract class Token[T](range: CodeRange, val value: T)
  extends CodeRange(range)
{
  /**
   * Returns the type of this token. Default implementation uses the name of
   * implementing class with "Token" removed.
   */
  def getType: String = getClass.getSimpleName
    .getClass
    .getSimpleName
    .replaceAll("Token", "")


  /**
   * A short description of this token. Default implementation returns the
   * type and value of this token.
   */
  def shortDescription: String = s"${getType.toLowerCase} '$value'"


  override def toString: String =
    s"$getType@${range.begin.getRow}:${range.begin.getCol}('$value')"
}