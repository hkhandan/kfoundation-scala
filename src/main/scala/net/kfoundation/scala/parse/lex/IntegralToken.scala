package net.kfoundation.scala.parse.lex

import net.kfoundation.scala.parse.CodeRange

class IntegralToken(range: CodeRange, value: Long)
  extends NumericToken[Long](range, value)
{
  def intValue: Int = value.toInt
  def asDecimalToken: DecimalToken = new DecimalToken(range, value)
}