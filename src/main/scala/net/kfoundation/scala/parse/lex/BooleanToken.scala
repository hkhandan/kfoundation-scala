package net.kfoundation.scala.parse.lex

import net.kfoundation.scala.parse.CodeRange

class BooleanToken(range: CodeRange, value: Boolean)
  extends Token[Boolean](range, value)