package net.kfoundation

import net.kfoundation.lang.lex.{CodeWalker, KeywordToken}
import org.scalatest.flatspec.AnyFlatSpec



class KeywordTokenTest extends AnyFlatSpec {

  object reader extends KeywordToken.Reader {
    val KW1: UString = add("kwa")
    val KW2: UString = add("kwb")
  }

  private def parse(s: UString): Option[KeywordToken] =
    reader.tryRead(CodeWalker.of(s))

  "Reader" should "contain all values" in {
    val values = reader.getValues
    assert(values.contains(reader.KW1))
    assert(values.contains(reader.KW2))
    assert(values.size == 2)
  }

  "Valid keyword" should "be parsed" in {
    assert(parse("kwa").map(_.value).contains(reader.KW1))
    assert(parse("kwb").map(_.value).contains(reader.KW2))
  }

  "Invalid keyword" should "not be parsed" in {
    assert(parse("nonekw").isEmpty)
    assert(parse("kw1234").isEmpty)
  }

  "String starting with valid keyword" should "be parsed" in {
    assert(parse("kwa 123").map(_.value).contains(reader.KW1))
    assert(parse("kwb abcd").map(_.value).contains(reader.KW2))
  }
}
