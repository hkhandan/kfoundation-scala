package net.kfoundation.scala.io

import net.kfoundation.scala.UString.Interpolator
import net.kfoundation.scala.parse.ParseError
import net.kfoundation.scala.util.WQName
import org.scalatest.funsuite.AnyFunSuite

class DomainTest extends AnyFunSuite {

  test("Fail on Empty String") {
    assertThrows[ParseError](WQName(""))
  }

  test("Parse Single-Part") {
    assert(WQName("the-single-part").parts == Seq(U"the-single-part") )
  }

  test("Parse Multi-Part") {
    assert(WQName("part1.part2.xyz").parts == Seq(U"part1", U"part2", U"xyz"))
  }

}
