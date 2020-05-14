package net.kfoundation.lang.lex

import net.kfoundation.lang.CodeRange

abstract class Token[T](range: CodeRange, val value: T)
  extends CodeRange(range)
{
  def getType: String = getClass.getSimpleName
    .getClass
    .getSimpleName
    .replaceAll("Token", "")

  def shortDescription: String = s"${getType.toLowerCase} '$value'"

  override def toString: String =
    s"${getType}@${range.begin.getRow}:${range.begin.getCol}('$value')"
}
