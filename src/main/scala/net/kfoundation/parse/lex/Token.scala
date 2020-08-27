package net.kfoundation.parse.lex

import net.kfoundation.parse.CodeRange

abstract class Token[T](range: CodeRange, val value: T)
  extends CodeRange(range)
{
  def getType: String = getClass.getSimpleName
    .getClass
    .getSimpleName
    .replaceAll("Token", "")

  def shortDescription: String = s"${getType.toLowerCase} '$value'"

  override def toString: String =
    s"$getType@${range.begin.getRow}:${range.begin.getCol}('$value')"
}
