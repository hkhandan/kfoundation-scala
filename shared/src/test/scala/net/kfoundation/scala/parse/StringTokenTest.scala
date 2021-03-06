// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse

import net.kfoundation.scala.UString
import net.kfoundation.scala.parse.lex.{CodeWalker, LexicalError, StringToken}
import org.scalatest.flatspec.AnyFlatSpec

class StringTokenTest extends AnyFlatSpec {
  private def parse(s: UString): Option[StringToken] =
    StringToken.reader.tryRead(CodeWalker.of(s))

  "Valid string token" should "be parsed" in {
    val testCases: Seq[(UString, UString)] = Seq(
      ("\"test 1234 '()'\"", "test 1234 '()'"),
      ("\"test \\n \\t \\b \\r 1234\"", "test \n \t \b \r 1234"),
      ("\"test \\\" 1234\"", "test \" 1234"),
      ("\"test \\\\ 1234\"", "test \\ 1234"))

    testCases.foreach(item =>
      assert(parse(item._1).map(_.value).contains(item._2), item))
  }

  "Invalid string tokens" should "not be parsed" in {
    val testCases = Seq(
      " \"String\"",
      "'string'",
      "1234",
      "abcd")

    testCases.foreach(str => assert(parse(str).isEmpty))
  }

  "Missing closing quote" should "cause error" in {
    assertThrows[LexicalError](parse("\"something"))
  }

  "Bad escape sequence" should "cause error" in {
    val th = intercept[LexicalError](parse("\"test \\g\""))
    assert(th.getMessage ==
      "[$buffer@1:9] Invalid escape sequence '\\g'")
  }
}
