package net.kfoundation

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}
import java.nio.charset.StandardCharsets

import net.kfoundation.UChar._

import scala.annotation.tailrec
import scala.language.implicitConversions


object UString {
  private class CodePointIterator(private val octets: Seq[Byte]) extends Iterator[Int] {
    private val reader = new ByteArrayUtf8Reader(octets)
    private var ch = reader.nextCodePoint

    override def hasNext: Boolean = ch != -1

    override def next(): Int = {
      val v = ch
      ch = reader.nextCodePoint
      v
    }
  }

  private class UCharIterator(private val octets: Seq[Byte]) extends Iterator[UChar] {
    private val it: CodePointIterator = new CodePointIterator(octets)

    override def hasNext: Boolean = it.hasNext

    override def next(): UChar = {
      val v = it.next()
      if(v < 0)
        null
      else
        new UChar(v)
    }
  }

  implicit class UStringInterpolation(ctx: StringContext) {
    object U {
      def apply(expr: Any*): UString = new UString(ctx.s(expr:_*))
    }
  }

  private val NULL = new UString("null")
  val EMPTY: UString = new UString(Array.empty[Byte])

  private def wrap[T](a: Array[T]): Seq[T] = a

  private def validateUtf8(bytes: Array[Byte]): Boolean = {
    val n = bytes.length
    var i = 0
    while(i < n) {
      i += getUtf8SizeWithFirstOctet(bytes(i))
    }
    i == n
  }

  def readUtf8(input: InputStream, nOctets: Int): UString = {
    val octets = input.readNBytes(nOctets)
    if(octets.length < nOctets) {
      throw new DecodingException(
        s"Not enough bytes to read. Expected:$nOctets, Actual: ${octets.length}")
    }
    if(!validateUtf8(octets)) {
      throw new DecodingException("Input is not valid UTF-8")
    }
    new UString(octets)
  }

  implicit def of(str: String): UString = new UString(str)

  def of(octets: Seq[Byte]) = new UString(octets.toArray)

  def of(octets: Seq[Byte], offset: Int, size: Int): UString = {
    val selection = new Array[Byte](size)
    octets.drop(offset).copyToArray(selection)
    new UString(selection)
  }

  def join(seq: Seq[UString], delimiter: UString): UString = {
    val size = seq.foldLeft(0)((a, b) => a + b.getUtf8Length) +
      delimiter.getUtf8Length*Math.max(seq.length-1, 0)
    val output = new ByteArrayOutputStream(size)
    val it = seq.iterator
    while(it.hasNext) {
      output.write(it.next().octets)
      if(it.hasNext) {
        output.write(delimiter.octets)
      }
    }
    new UString(output.toByteArray)
  }
}


class UString private (private val octets: Array[Byte]) {
  import UString._

  private var length: Integer = null

  def this(nativeString: String) = this(
    nativeString.getBytes(StandardCharsets.UTF_8))

  private def codePointIterator: Iterator[Int] =
    new CodePointIterator(wrap(octets))

  @tailrec
  private def getCharCount(n: Int, offset: Int, size: Int): Int =
    if(offset < octets.length) {
      getCharCount(n, offset + UChar.getUtf8SizeWithFirstOctet(octets(offset)), size+1)
    } else {
      size
    }

  @tailrec
  private def find(target: Seq[Byte], offset: Int, i: Int, cp: Int): Int =
    if(offset >= octets.length) {
      -1
    } else if(offset + i >= octets.length) {
      find(target, offset + 1, 0, cp + codePointInc(offset))
    } else if(i == target.length) {
      cp
    } else if(target(i) == octets(offset + i)) {
      find(target, offset, i + 1, cp)
    } else {
      find(target, offset + 1, 0, cp + codePointInc(offset))
    }

  private def codePointInc(offset: Int): Int =
    if(UChar.getUtf8SizeWithFirstOctet(octets(offset)) > 0) 1 else 0

  private def locationOfCodePointAtIndex(index: Int): Int =
    locationOfCodePointAtIndex(0, 0, index)

  @tailrec
  private def octetsEqual(i: Int, thatOctets: Array[Byte]): Boolean =
    if(i <= 0) {
      true
    } else if(octets(i) == thatOctets(i)) {
      octetsEqual(i - 1, thatOctets)
    } else {
      false
    }

  @tailrec
  private def locationOfCodePointAtIndex(offset: Int, i: Int, index: Int): Int =
    if(offset >= octets.length) {
      throw new IndexOutOfBoundsException(index.toString + " (string length: " + getLength + ")")
    } else if(i < index) {
      locationOfCodePointAtIndex(offset + UChar.getUtf8SizeWithFirstOctet(octets(offset)), i + 1, index)
    } else {
      offset
    }

  private def find(target: Seq[Byte], index: Int): Int =
    find(target, locationOfCodePointAtIndex(index), 0, index)

  @tailrec
  private def equalsIgnoreCase(thisReader: Utf8Reader, thatReader: Utf8Reader): Boolean = {
    val thisChar = thisReader.nextCodePoint
    val thatChar = thatReader.nextCodePoint
    if (thisChar == -1 || thatChar == -1) {
      thisChar == thatChar
    } else if (UChar.toLowerCase(thisChar) == UChar.toLowerCase(thatChar)) {
      equalsIgnoreCase(thisReader, thatReader)
    } else {
      false
    }
  }

  def toUtf8: Seq[Byte] = wrap(octets)

  def octetsIterator: Iterator[Byte] = octets.iterator

  def uCharIterator: Iterator[UChar] = new UCharIterator(wrap(octets))

  def getLength: Int = {
    if(length == null) {
      length = getCharCount(octets.length, 0, 0)
    }
    length
  }

  def getUtf8Length: Int = octets.length

  def isEmpty: Boolean = octets.isEmpty

  def equalsIgnoreCase(that: UString): Boolean = equalsIgnoreCase(
    new ByteArrayUtf8Reader(wrap(this.octets)),
    new ByteArrayUtf8Reader(wrap(that.octets)))

  @tailrec
  final def find(octet: Byte, offset: Int): Int =
    if(offset >= octets.length) {
      -1
    } else if(octets(offset) == octet) {
      offset
    } else {
      find(octet, offset + 1)
    }

  def find(char: UChar, offset: Int): Int = find(char.toUtf8, offset)

  def find(str: UString, offset: Int): Int = find(wrap(str.octets), offset)

  def mapCodePoints(fn: Int => Int): UString = new UString(
    codePointIterator.map(fn)
      .foldLeft(
        new BufferUtf8Writer())(
        (w, ch) => {w.write(ch); w})
      .get)

  def toLowerCase: UString = mapCodePoints(UChar.toLowerCase)

  def toUpperCase: UString = mapCodePoints(UChar.toUpperCase)

  def toFirstUpperCase: UString = {
    val writer = new BufferUtf8Writer()
    val it = codePointIterator
    writer.write(UChar.toUpperCase(it.next()))
    it.foreach(ch => writer.write(ch))
    new UString(writer.get)
  }

  def subString(begin: Int, end: Int): UString = {
    val l1 = locationOfCodePointAtIndex(0, 0, begin)
    val l2 = locationOfCodePointAtIndex(l1, begin, end)
    val size = l2 - l1
    val result = new Array[Byte](size)
    Array.copy(octets, l1, result, 0, size)
    new UString(result)
  }

  def subString(begin: Int): UString = {
    val l = locationOfCodePointAtIndex(0, 0, begin)
    val size = octets.length - l
    val result = new Array[Byte](size)
    Array.copy(octets, l, result, 0, size)
    new UString(result)
  }

  def append(str: UString): UString = append(str.octets)

  def append(raw: Seq[Byte]): UString = new UString(octets ++ raw)

  def append(ch: UChar): UString = append(ch.toUtf8)

  def printToStream(os: OutputStream): Unit = os.write(octets)

  def +(v: UString): UString = append(v)

  def +(o: Any): UString = append(
    if(o == null) NULL else new UString(o.toString))

  override def toString: String = new String(octets, StandardCharsets.UTF_8)

  override def hashCode(): Int = MurmurHash3.hash32x86(octets)

  override def equals(other: Any): Boolean = other match {
    case that: UString => (this eq that) ||
      this.octets.length == that.octets.length &&
      octetsEqual(octets.length - 1, that.octets)
    case _ => false
  }
}