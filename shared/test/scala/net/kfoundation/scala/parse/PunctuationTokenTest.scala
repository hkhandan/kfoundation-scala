// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse

import net.kfoundation.scala.parse.lex.{CodeWalker, PunctuationToken}
import net.kfoundation.scala.{UChar, UString}
import org.scalatest.flatspec.AnyFlatSpec

class PunctuationTokenTest extends AnyFlatSpec {

  object reader extends PunctuationToken.Reader {
    val P1: UChar = add('?')
    val P2: UChar = add('!')
  }

  private def parse(s: UString): Option[PunctuationToken] =
    reader.tryRead(CodeWalker.of(s))

  "Reader" should "contain all values" in {
    val values = reader.getValues
    assert(values.contains(reader.P1))
    assert(values.contains(reader.P2))
    assert(values.size == 2)
  }

  "Valid operator" should "be parsed" in {
    assert(parse("?").map(_.value).contains(reader.P1))
    assert(parse("!").map(_.value).contains(reader.P2))
  }

  "Invalid operator" should "not be parsed" in {
    assert(parse(";").isEmpty)
  }

  "String starting with valid operator" should "be parsed" in {
    assert(parse("?23").map(_.value).contains(reader.P1))
    assert(parse("!abcd").map(_.value).contains(reader.P2))
  }

}
