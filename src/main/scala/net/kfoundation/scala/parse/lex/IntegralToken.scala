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



/** A portion of input text that represents an integral number */
class IntegralToken(range: CodeRange, value: Long)
  extends NumericToken[Long](range, value)
{
  def intValue: Int = value.toInt
  def asDecimalToken: DecimalToken = new DecimalToken(range, value.toDouble)
}