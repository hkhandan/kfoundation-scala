package net.kfoundation.lang.lex

import net.kfoundation.lang.CodeRange

class IntegralToken(range: CodeRange, value: Long)
  extends NumericToken[Long](range, value)
{
  def intValue: Int = value.toInt
}