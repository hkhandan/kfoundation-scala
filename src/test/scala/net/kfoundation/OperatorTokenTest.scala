package net.kfoundation

import net.kfoundation.lang.lex.{CodeWalker, OperatorToken}
import org.scalatest.flatspec.AnyFlatSpec

class OperatorTokenTest extends AnyFlatSpec {

  object reader extends OperatorToken.Reader {
    val OP1: UString = add("<>")
    val OP2: UString = add("==")
  }

  private def parse(s: UString): Option[OperatorToken] =
    reader.tryRead(CodeWalker.of(s))

  "Reader" should "contain all values" in {
    val values = reader.getValues
    assert(values.contains(reader.OP1))
    assert(values.contains(reader.OP2))
    assert(values.size == 2)
  }

  "Valid operator" should "be parsed" in {
    assert(parse("<>").map(_.value).contains(reader.OP1))
    assert(parse("==").map(_.value).contains(reader.OP2))
  }

  "Invalid operator" should "not be parsed" in {
    assert(parse("=").isEmpty)
  }

  "String starting with valid operator" should "be parsed" in {
    assert(parse("<>123").map(_.value).contains(reader.OP1))
    assert(parse("==abcd").map(_.value).contains(reader.OP2))
  }

}
