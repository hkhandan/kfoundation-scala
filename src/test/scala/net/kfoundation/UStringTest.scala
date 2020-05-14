package net.kfoundation

import java.io.ByteArrayInputStream

import org.scalatest.funsuite.AnyFunSuite

class UStringTest extends AnyFunSuite {
  import UString._

  private def byteSeq(list: Int*): Seq[Byte] = list.map(_.toByte)
  private def bytes(list: Int*): Array[Byte] = byteSeq(list:_*).toArray
  private val testUtf8 = bytes(0x61, 0xE3, 0x80, 0x80, 0xf0, 0x90, 0x90, 0xb7)
  private val a = new UChar('a')
  private val full_width_space = new UChar('　')
  private val rare_kanji = new UChar(0x10437)

  test("UString interpolation") {
    val a = 24
    val b = "test string"
    val c = Some(b)
    assert(U"values: $a $b $c" ==
      UString.of("values: 24 test string Some(test string)"))
  }

  test("UString from native String") {
    val str = new String(Array[Int](0x61, 0x3000, 0x10437), 0, 3)
    assert(UString.of(str).toUtf8 == testUtf8.toSeq)

    // Implicit conversion
    val uStr: UString = str
    assert(uStr.toUtf8 == testUtf8.toSeq)
  }

  test("UString from byte array") {
    assert(UString.of(testUtf8).toUtf8 == testUtf8.toSeq)
  }

  test("UString from sub array of bytes") {
    val b = bytes('a', 'b', 'c', 'd', 'e', 'f')
    assert(UString.of(b, 2, 3).toUtf8 == byteSeq('c', 'd', 'e'))
  }

  test("UString from UTF-8 stream") {
    val s = new ByteArrayInputStream(testUtf8)
    assert(UString.readUtf8(s, testUtf8.length).toUtf8 == testUtf8.toSeq)

    val bogous = new ByteArrayInputStream(testUtf8.dropRight(1))
    assertThrows[DecodingException](UString.readUtf8(s, testUtf8.length-1))

    s.reset()
    val th = intercept[DecodingException](
      UString.readUtf8(s, testUtf8.length + 2))
    assert(th.getMessage == "Not enough bytes to read. Expected:10, Actual: 8")
  }

  test("toUtf8") {
    assert(UString.of(testUtf8).toUtf8 == testUtf8.toSeq)
  }

  test("octetsIterator") {
    val it = UString.of(testUtf8).octetsIterator
    assert(testUtf8.forall(b => b == it.next()))
    assert(!it.hasNext)
  }

  test("uCharIterator") {
    val it = UString.of(testUtf8).uCharIterator
    assert(it.next() == a)
    assert(it.next() == full_width_space)
    assert(it.next() == rare_kanji)
    assert(!it.hasNext)
  }

  test("getLength") {
    assert(UString.of(testUtf8).getLength == 3)
    assert(U"abcdef".getLength == 6)
  }

  test("getUtf8Length") {
    assert(UString.of(testUtf8).getUtf8Length == testUtf8.length)
    assert(U"abcdef".getUtf8Length == 6)
  }

  test("isEmpty") {
    assert(U"".isEmpty)
    assert(UString.EMPTY.isEmpty)
    assert(!U"a".isEmpty)
  }

  test("equalsIgnoreCase") {
    assert(U"abcdef".equalsIgnoreCase(U"AbCdEf"))
    assert(U"テストabcdef漢字".equalsIgnoreCase(U"テストAbCdEf漢字"))
  }

  test("find") {
    val str = U"テストabcdef漢字abcdef"
    assert(str.find(0x61.toByte, 0) == 9)
    assert(str.find(0x61.toByte, 10) == 21)
    assert(str.find(0x61.toByte, 22) == -1)

    assert(str.find(a, 0) == 3)
    assert(str.find(a, 4) == 11)
    assert(str.find(a, 12) == -1)
    assert(str.find(UChar.of('字'), 0) == 10)

    assert(str.find(U"abcd", 0) == 3)
    assert(str.find(U"abc", 4) == 11)
    assert(str.find(U"漢字", 0) == 9)
    assert(str.find(U"テスト", 0) == 0)
    assert(str.find(UString.EMPTY, 0) == 0)
    assert(str.find(U"xyz", 0) == -1)
  }

  test("toLowerCase") {
    assert(U"テストAbCdEf".toLowerCase == U"テストabcdef")
  }

  test("toUpperCase") {
    assert(U"テストAbCdEf".toUpperCase == U"テストABCDEF")
  }

  test("subString") {
    val str = U"テストabcdef漢字abcdef"
    assert(str.subString(3) == U"abcdef漢字abcdef")
    assert(str.subString(3, 6) == U"abc")
  }

  test("append") {
    assert(U"テスト".append(U"abcdef") == U"テストabcdef")
    assert(U"テスト" + U"abcdef" == U"テストabcdef")
    assert(U"テスト" + 123.45 == U"テスト123.45")
  }
}
