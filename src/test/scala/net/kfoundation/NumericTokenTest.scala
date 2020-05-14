package net.kfoundation

import net.kfoundation.lang.lex.{CodeWalker, DecimalToken, IntegralToken, NumericToken}
import org.scalatest.flatspec.AnyFlatSpec

class NumericTokenTest extends AnyFlatSpec {
  import net.kfoundation.UString._

  private def parse(s: UString): Option[NumericToken[_]] =
    NumericToken.reader.tryRead(CodeWalker.of(s))


  "Valid integers" should "be parsed as IntegralToken" in {
    val testCases = Seq[(UString, Int)](
      ("1234", 1234),
      ("0001234000", 1234000),
      ("000", 0),
      ("-0", 0),
      ("+0", 0),
      ("-1234", -1234),
      ("+1234", 1234),
      ("-00001234", -1234))

    testCases.foreach(
      item => assert(parse(item._1)
        .map(_.asInstanceOf[IntegralToken].value)
        .contains(item._2)))
  }

  "Valid real numbers" should "be parsed as DecimalToken" in {
    val testCases = Seq[(UString, Double)](
      ("12.34", 12.34),
      (".1234", 0.1234),
      ("0.1234", 0.1234),
      ("000.1234", 0.1234),
      ("0.1234000", 0.1234),
      ("+12.34", 12.34),
      ("+.1234", 0.1234),
      ("-12.34", -12.34),
      ("-.1234", -.1234),
      ("1.234e2", 123.4),
      ("1.234e+2", 123.4),
      ("1.234e-2", 0.01234),
      ("1.234E2", 123.4),
      ("1.234E0002", 123.4),
      ("1.2340000E2", 123.4))

    testCases.foreach(item =>
      assert(parse(item._1)
        .map(_.asInstanceOf[DecimalToken].floatValue)
        .contains(item._2.toFloat)))
  }

  "Invalid numbers" should "not be parsed" in {
    val invalidValues = Seq[UString](
      "abcd1234",
      "..1234",
      "E1234",
      "-",
      "+")
    invalidValues.foreach(item =>
      assert(parse(item).isEmpty, item))
  }

  "String beginning with valid decimal" should "parse into decimal" in {
    val testCases = Seq[(UString, Double)](
      ("12.34.abcd", 12.34),
      (".1234 ", 0.1234),
      ("0.1234 another word", 0.1234),
      ("1.234e2emphaty", 123.4),
      ("1.234e-2-2", 0.01234),
      ("1.234E2+3", 123.4))

    testCases.foreach(item =>
      assert(parse(item._1)
        .map(_.asInstanceOf[DecimalToken].floatValue)
        .contains(item._2.toFloat)))
  }

  "String beginning with valid integer" should "parse into integer" in {
    val testCases = Seq[(UString, Int)](
      ("1234.abc", 1234),
      ("0001234000 ", 1234000),
      ("000 another word", 0),
      ("-0", 0),
      ("-1234-2", -1234),
      ("+1234+3", 1234))

    testCases.foreach(
      item => assert(parse(item._1)
        .map(_.asInstanceOf[IntegralToken].value)
        .contains(item._2)))
  }


}
