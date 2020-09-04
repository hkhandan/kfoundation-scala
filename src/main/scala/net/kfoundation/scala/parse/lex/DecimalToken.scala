package net.kfoundation.scala.parse.lex

import net.kfoundation.scala.parse.CodeRange

class DecimalToken(range: CodeRange, value: Double)
  extends NumericToken[Double](range, value)
{
  def floatValue: Float = value.toFloat
}