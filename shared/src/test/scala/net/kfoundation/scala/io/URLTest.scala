package net.kfoundation.scala.io

import net.kfoundation.scala.UString.Interpolator
import net.kfoundation.scala.io.URL.Authority
import net.kfoundation.scala.util.WQName
import org.scalatest.funsuite.AnyFunSuite

class URLTest extends AnyFunSuite {

  test("Parse Simple Address") {
    val expected = URL("scheme", Authority(Seq(U"host")), Path.ROOT)
    assert(URL("scheme://host") == expected)
    assert(URL("scheme://host/") == expected)
  }

  test("Parse Compound Domain") {
    val expected = URL("scheme",
      Authority(Seq(U"host", U"domain", U"com")),
      Path.ROOT)
    assert(URL("scheme://host.domain.com") == expected)
    assert(URL("scheme://host.domain.com/") == expected)
  }

  test("Parse User and Port") {
    val expected = URL("scheme",
      Authority(
        WQName(Seq(U"host", U"domain", U"com")),
        Some(123), Some(U"user")),
      Path.ROOT)
    assert(URL("scheme://user@host.domain.com:123") == expected)
    assert(URL("scheme://user@host.domain.com:123/") == expected)
  }

  test("Parse Path") {
    val expected = URL("scheme", Authority(Seq(U"host")),
      Path.absolute("path", "to", "location"))
    assert(URL("scheme://host/path/to/location") == expected)
    assert(URL("scheme://host/path/to/location/") == expected)
  }

  test("Parse Encoded Path") {
    val expected = URL("scheme", Authority(Seq(U"host")),
      Path.absolute(U"上海+中國+abcd", U"hgh"))
    assert(
      URL("scheme://host/%E4%B8%8A%E6%B5%B7%2B%E4%B8%AD%E5%9C%8B+abcd/hgh")
        == expected)
  }

  test("Parse Query") {
    val expected = URL("scheme", Authority(Seq(U"host")),
      Path.absolute(U"path"), URL.Query(
        U"param1"->U"",
        U"param2"->U"",
        U"param3"->U"value3"))
    val input = URL("scheme://host/path/?param1&param2=&param3=value3")
    input.path.segments.zipWithIndex.foreach(println)
    assert(input == expected)
  }

  test("Parse Encoded Query") {
    val expected = URL("scheme", Authority(Seq(U"host")),
      Path.absolute(U"path"), URL.Query(U"param"->U"上海+中國"))
    val input = URL("scheme://host/path/?param=%E4%B8%8A%E6%B5%B7%2B%E4%B8%AD%E5%9C%8B")
    assert(input == expected)
  }

  test("Parse Fragment") {
    val expected1 = URL("scheme", Authority(Seq(U"host")),
      Path.ROOT, URL.NO_QUERY, Some(U"fragment"))
    assert(URL("scheme://host#fragment") == expected1)
    assert(URL("scheme://host/#fragment") == expected1)

    val expected2 = URL("scheme", Authority(Seq(U"host")),
      Path.absolute(U"path"), URL.NO_QUERY, Some(U"fragment"))
    assert(URL("scheme://host/path#fragment") == expected2)
    assert(URL("scheme://host/path/#fragment") == expected2)
  }

  test("Encode") {
    val input = URL("scheme", Authority(Seq(U"host")),
      Path.absolute(U"path"), URL.Query(U"param"->U"上海+中國"))
    val expected = U"scheme://host/path?param=%E4%B8%8A%E6%B5%B7%2B%E4%B8%AD%E5%9C%8B"
    assert(input.encoded == expected)
  }

  test("Parse Unicode") {
    URL("http://uref.io/❣️")
  }
}
