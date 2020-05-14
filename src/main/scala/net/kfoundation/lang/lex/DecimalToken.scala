package net.kfoundation.lang.lex

import net.kfoundation.lang.CodeRange

class DecimalToken(range: CodeRange, value: Double)
  extends NumericToken[Double](range, value)
{
  def floatValue: Float = value.toFloat
}