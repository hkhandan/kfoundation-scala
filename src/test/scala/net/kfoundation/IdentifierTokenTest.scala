package net.kfoundation

import net.kfoundation.lang.CodeRange
import net.kfoundation.lang.lex.{CodeWalker, IdentifierToken}


class IdentifierTokenTest extends org.scalatest.flatspec.AnyFlatSpec {
  import UString._

  private def parse(s: UString): Option[IdentifierToken] =
    IdentifierToken.reader.tryRead(CodeWalker.of(s))

  "An empty string" should "not be an identifier" in {
    assert(parse("").isEmpty)
  }

  "Single letter" should "be an identifier" in {
    val expected = new IdentifierToken(new CodeRange("UString", 1, 1, 1), "a")
    assert(parse("a").contains(expected))
  }

  "Valid java identifier" should "be identifier" in {
    val validValues = Seq(
      U"MyVariable",
      U"MYVARIABLE",
      U"myvariable",
      U"x",
      U"i",
      U"_myvariable",
      U"$$myvariable",
      U"sum_of_numbers",
      U"edureka123")

    assert(validValues.forall(v => parse(v).map(id => id.value).contains(v)))
  }

  "Invalid java identifier" should "not be identifier" in {
    val invalidValue = Seq(
      U"My Variable",
      U"456edureka",
      U"c+d",
      U"variable-5",
      U"add_&_sub")

    assert(invalidValue.forall(v => !parse(v).map(id => id.value).contains(v)))
    assert(parse("+abc").isEmpty)
    assert(parse("123abc").isEmpty)
  }

  "String starting with ID" should "have ID parsed" in {
    assert(parse("My Variable").map(_.value).contains(U"My"))
    assert(parse("c+d").map(_.value).contains(U"c"))
    assert(parse("variable-5").map(_.value).contains(U"variable"))
    assert(parse("add_&_sub").map(_.value).contains(U"add_"))
  }
}
