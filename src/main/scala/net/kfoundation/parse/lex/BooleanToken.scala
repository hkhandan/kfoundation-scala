package net.kfoundation.parse.lex

import net.kfoundation.parse.CodeRange

class BooleanToken(range: CodeRange, value: Boolean)
  extends Token[Boolean](range, value)