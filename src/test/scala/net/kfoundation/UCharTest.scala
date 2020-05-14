package net.kfoundation

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import net.kfoundation.UChar.{ArrayUtf8Writer, ByteArrayUtf8Reader, SingleUtf8Writer, StreamUtf8Reader, StreamUtf8Writer}
import org.scalatest.funsuite.AnyFunSuite


class UCharTest extends AnyFunSuite {

  private val a = new UChar('a')
  private val capital_a = new UChar('A')
  private val minus = new UChar('-')
  private val three = new UChar('3')
  private val space = new UChar(' ')
  private val full_width_space = new UChar('　')
  private val rare_kanji = new UChar(0x10437)
  private val testUtf8 = bytes(0x61, 0xE3, 0x80, 0x80, 0xf0, 0x90, 0x90, 0xb7)

  private def byteSeq(list: Int*): Seq[Byte] = list.map(_.toByte)
  private def bytes(list: Int*): Array[Byte] = byteSeq(list:_*).toArray


  test("isLowerCase") {
    assert(a.isLowerCase)
    assert(!capital_a.isLowerCase)
    assert(!minus.isLowerCase)
  }

  test("isUpperCase") {
    assert(!a.isUpperCase)
    assert(capital_a.isUpperCase)
    assert(!minus.isLowerCase)
  }

  test("isNumeric") {
    assert(!a.isNumeric)
    assert(!minus.isNumeric)
    assert(three.isNumeric)
  }

  test("isAlphabet") {
    assert(a.isAlphabet)
    assert(capital_a.isAlphabet)
    assert(!minus.isAlphabet)
    assert(!three.isAlphabet)
  }

  test("isAlphanumeric") {
    assert(a.isAlphanumeric)
    assert(capital_a.isAlphanumeric)
    assert(three.isAlphanumeric)
    assert(!minus.isAlphanumeric)
  }

  test("isWhiteSpace") {
    assert(!a.isWhiteSpace)
    assert(!minus.isWhiteSpace)
    assert(space.isWhiteSpace)
    assert(full_width_space.isWhiteSpace)
  }

  test("toLowerCase") {
    assert(capital_a.toLowerCase == a)
  }

  test("toUpperCase") {
    assert(a.toUpperCase == capital_a)
  }

  test("getUtf8Length") {
    assert(a.getUtf8Length == 1)
    assert(full_width_space.getUtf8Length == 3)
  }

  test("toUtf8") {
    assert(a.toUtf8 == byteSeq(0x61))
    assert(full_width_space.toUtf8 == byteSeq(0xE3, 0x80, 0x80))
    assert(rare_kanji.toUtf8 == byteSeq(0xf0, 0x90, 0x90, 0xb7))
  }

  test("toUtf16") {
    assert(a.toUtf16 == Seq(0x61))
    assert(full_width_space.toUtf16 == Seq(0x3000))
    assert(rare_kanji.toUtf16 == Seq(0xD801, 0xDC37))
  }

  test("appendTo") {
    val b = new StringBuilder("b")
    a.appendTo(b)
    assert(b.toString() == "ba")
  }

  test("hashCode") {
//    assert(a.hashCode() == MurmurHash3.bytesHash(bytes(0x61), 0))
//    assert(full_width_space.hashCode() == MurmurHash3.bytesHash(bytes(0xE3, 0x80, 0x80)))
  }

  test("toString") {
    assert(a.toString == "a")
    assert(full_width_space.toString == "　")
    assert(rare_kanji.toString == new String(Array[Int](0x10437), 0, 1))
  }

  test("equals") {
    assert(!a.equals(capital_a))
    assert(!a.equals(None))
    assert(a.equals(new UChar(0x61)))
    assert(full_width_space.equals(new UChar(0x3000)))
  }

  test("ArrayUtf8Writer") {
    val buffer = new Array[Byte](40)

    val writer = new ArrayUtf8Writer(buffer, 10)
    writer.write(a.codePoint)
    writer.write(full_width_space.codePoint)
    writer.write(rare_kanji.codePoint)

    val expected = Array.fill(10)(0).appendedAll(testUtf8)

    assert(writer.getOffset == expected.length)
    assert(expected.indices.forall(i => buffer(i) == expected(i)))
  }

  test("StreamUtf8Writer") {
    val os = new ByteArrayOutputStream()
    val writer = new StreamUtf8Writer(os)
    writer.write(a.codePoint)
    writer.write(full_width_space.codePoint)
    writer.write(rare_kanji.codePoint)
    assert(os.toByteArray.sameElements(testUtf8))
  }

  test("SingleUtf8Writer") {
    val writer = new SingleUtf8Writer()
    writer.write(a.codePoint)
    writer.write(rare_kanji.codePoint)
    assert(writer.get.sameElements(byteSeq(0xf0, 0x90, 0x90, 0xb7)))
  }

  test("ByteArrayUtf8Reader") {
    val reader = new ByteArrayUtf8Reader(testUtf8)
    assert(reader.nextUChar.contains(a))
    assert(reader.getNumberOfLastOctetsRead == 1)

    assert(reader.nextUChar.contains(full_width_space))
    assert(reader.getNumberOfLastOctetsRead == 3)

    assert(reader.nextUChar.contains(rare_kanji))
    assert(reader.getNumberOfLastOctetsRead == 4)

    assert(!reader.hasMore)
    assert(reader.nextCodePoint == -1)
    assert(reader.getStreamPosition == 8)
  }

  test("StreamUtf8Reader") {
    val reader = new StreamUtf8Reader(
      new ByteArrayInputStream(testUtf8))

    assert(reader.nextUChar.contains(a))
    assert(reader.getNumberOfLastOctetsRead == 1)

    assert(reader.nextUChar.contains(full_width_space))
    assert(reader.getNumberOfLastOctetsRead == 3)

    assert(reader.nextUChar.contains(rare_kanji))
    assert(reader.getNumberOfLastOctetsRead == 4)

    assert(reader.nextCodePoint == -1)
  }

}
