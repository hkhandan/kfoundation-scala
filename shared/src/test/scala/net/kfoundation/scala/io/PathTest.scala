package net.kfoundation.scala.io

import net.kfoundation.scala.UString.Interpolator
import org.scalatest.funsuite.AnyFunSuite

class PathTest extends AnyFunSuite {

  test("Parse Empty Path") {
    assert(Path("") == Path.EMPTY)
  }

  test("Parse Root") {
    assert(Path("/") == Path.ROOT)
  }

  test("Parse Relative Path") {
    val p = Path("abc/efgh/ijkl")
    assert(p.isRelative)
    assert(p.segments == Seq(U"abc", U"efgh", U"ijkl"))
  }

  test("Parse Absolute Path") {
    val p = Path("/abc/defg/hijk")
    assert(!p.isRelative)
    assert(p.segments == Seq(U"abc", U"defg", U"hijk"))
  }

  test("Parse Encoded Path") {
    assert(
      Path("/%E4%B8%8A%E6%B5%B7%2B%E4%B8%AD%E5%9C%8B+abcd/hgh").segments ==
      Seq(U"上海+中國+abcd", U"hgh"))
  }

  test("Get File Name") {
    val p = Path("/abc/defg.hij")
    assert(p.fileName.contains(U"defg.hij"))
  }

  test("Get File Extension") {
    val p = Path("/abc/defg.hij.klm")
    assert(p.extension.contains(U"klm"))
  }

}
