package net.kfoundation.parse.lex

import net.kfoundation.parse.CodeRange

class DecimalToken(range: CodeRange, value: Double)
  extends NumericToken[Double](range, value)
{
  def floatValue: Float = value.toFloat
}