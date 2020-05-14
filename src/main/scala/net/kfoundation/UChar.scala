package net.kfoundation

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}

import scala.annotation.tailrec
import scala.language.implicitConversions


object UChar {
  trait Utf8Writer {
    private val buffer = new Array[Byte](6)

    def write(codePoint: Int): Unit = {
      val n = UChar.write(codePoint, buffer)
      writeBytes(buffer, n)
    }

    protected def writeBytes(bytes: Array[Byte], count: Int): Unit
  }

  class ArrayUtf8Writer(private val a: Array[Byte], private var offset: Int)
    extends Utf8Writer
  {
    def this(a: Array[Byte]) = this(a, 0)

    override protected def writeBytes(octets: Array[Byte], count: Int): Unit = {
      Array.copy(octets, 0, a, offset, count)
      offset += count
    }

    def getOffset: Int = offset
  }

  class StreamUtf8Writer(private val s: OutputStream) extends Utf8Writer {
    override protected def writeBytes(bytes: Array[Byte], count: Int): Unit =
      s.write(bytes, 0, count)
  }

  class BufferUtf8Writer private (private val s: ByteArrayOutputStream)
    extends StreamUtf8Writer(s)
  {
    def this() = this(new ByteArrayOutputStream())
    def get: Array[Byte] = s.toByteArray
  }

  class SingleUtf8Writer() extends Utf8Writer {
    private var value: Array[Byte] = _
    override def writeBytes(octets: Array[Byte], count: Int): Unit =
      value =  octets.slice(0, count)
    def get: Array[Byte] = value
  }

  trait Utf8Reader {
    private val buffer = new Array[Byte](6)
    private var delta = 0

    def nextOctet: Int

    def nextCodePoint: Int = fillBufferAndDecode(0, -1)

    def nextUChar: Option[UChar] = {
      val ch = nextCodePoint
      if (ch >= 0) {
        Some(new UChar(ch, buffer.slice(0, delta)))
      } else
        None
    }

    def getNumberOfLastOctetsRead: Int = delta

    @tailrec
    private def fillBufferAndDecode(i: Int, n: Int): Int = if(n == i) {
      decode(n - 1, n, 0, 0)
    } else {
      val next = nextOctet
      if(next < 0) {
        delta=0
        -1
      } else {
        buffer(i) = next.toByte
        if(n < 0) {
          fillBufferAndDecode(i + 1, getUtf8SizeWithFirstOctet(next))
        } else {
          fillBufferAndDecode(i + 1, n)
        }
      }
    }

    @tailrec
    private def decode(i: Int, n: Int, nBits: Int, output: Int): Int = if(i > 0) {
      decode(i - 1, n, nBits + 6, output | (buffer(i) & 0x3f) << nBits)
    } else {
      delta = n
      output | (getHighOctet(buffer(0), n) << nBits)
    }
  }

  class ByteArrayUtf8Reader(private val octets: Seq[Byte])
    extends Utf8Reader
  {
    private var pos: Int = 0

    override def nextOctet: Int = if(pos < octets.length) {
      val i = pos
      pos += 1
      0xFF & octets(i)
    } else {
      -1
    }

    def seek(newPos: Int): Boolean = if(newPos >= octets.length) {
      false
    } else {
      pos = newPos
      true
    }

    def getStreamPosition: Int = pos

    def hasMore: Boolean = pos < octets.length
  }

  class StreamUtf8Reader(private val stream: InputStream) extends Utf8Reader {
    override def nextOctet: Int = stream.read()
  }

  private val NXT : Int = 0x80
  private val SEQ2: Int = 0xc0
  private val SEQ3: Int = 0xe0
  private val SEQ4: Int = 0xf0
  private val SEQ5: Int = 0xf8
  private val SEQ6: Int = 0xfc

  private def wrap[T](a: Array[T]): Seq[T] = a

  private def write(codePoint: Int, n: Int, buffer: Array[Byte]): Unit = {
    val oc = decompose(codePoint)
    n match {
      case 1 =>
        buffer(0) = oc(0).toByte
      case 2 =>
        buffer(0) = (SEQ2 | (oc(0) >> 6) | ((oc(1) & 0x07) << 2)).toByte
        buffer(1) = (NXT | (oc(0) & 0x3f)).toByte
      case 3 =>
        buffer(0) = (SEQ3 | ((oc(1) & 0xf0) >> 4)).toByte
        buffer(1) = (NXT | (oc(0) >> 6) | ((oc(1) & 0x0f) << 2)).toByte
        buffer(2) = (NXT | (oc(0) & 0x3f)).toByte
      case 4 =>
        buffer(0) = (SEQ4 | ((oc(2) & 0x1f) >> 2)).toByte
        buffer(1) = (NXT | ((oc(1) & 0xf0) >> 4) | ((oc(2) & 0x03) << 4)).toByte
        buffer(2) = (NXT | (oc(0) >> 6) | ((oc(1) & 0x0f) << 2)).toByte
        buffer(3) = (NXT | (oc(0) & 0x3f)).toByte
      case 5 =>
        buffer(0) = (SEQ5 | (oc(3) & 0x03)).toByte
        buffer(1) = (NXT | (oc(2) >> 2)).toByte
        buffer(2) = (NXT | ((oc(1) & 0xf0) >> 4) | ((oc(2) & 0x03) << 4)).toByte
        buffer(3) = (NXT | (oc(0) >> 6) | ((oc(1) & 0x0f) << 2)).toByte
        buffer(4) = (NXT | (oc(0) & 0x3f)).toByte
      case 6 =>
        buffer(0) = (NXT | (oc(0) & 0x3f)).toByte
        buffer(1) = (NXT | (oc(0) >> 6) | ((oc(1) & 0x0f) << 2)).toByte
        buffer(2) = (NXT | (oc(1) >> 4) | ((oc(2) & 0x03) << 4)).toByte
        buffer(3) = (NXT | (oc(2) >> 2)).toByte
        buffer(4) = (NXT | (oc(3) & 0x3f)).toByte
        buffer(5) = (SEQ6 | ((oc(3) & 0x40) >> 6)).toByte
    }
  }

  private def write(codePoint: Int, buffer: Array[Byte]): Int = {
    val n = getUtf8SizeWithCodePoint(codePoint)
    write(codePoint, n, buffer)
    n
  }

  private def decompose(input: Int): Array[Int] = Array(
    input.toByte & 0xFF,
    (input >>> 8) & 0xFF,
    (input >>> 16) & 0xFF,
    (input >>> 28) & 0xFF)

  /**
   * Returns the number of octets for the codepoint starting with the given
   * octet.
   */
  def getUtf8SizeWithFirstOctet(firstOctet: Int): Byte =
    if ((firstOctet & 0x80) == 0) {
      1
    } else if ((firstOctet & 0xe0) == SEQ2) {
      2
    } else if ((firstOctet & 0xf0) == SEQ3) {
      3
    } else if ((firstOctet & 0xf8) == SEQ4) {
      4
    } else if ((firstOctet & 0xfc) == SEQ5) {
      5
    } else if ((firstOctet & 0xfe) == SEQ6) {
      6
    } else {
      0
    }


  /**
   * Returns the number of octets necessary to represent the given character
   * in UTF-8 format.
   */
  def getUtf8SizeWithCodePoint(codePoint: Int): Byte =
    if (codePoint <= 0x0000007f) {
      1
    } else if (codePoint <= 0x000007ff) {
      2
    } else if (codePoint <= 0x0000ffff) {
      3
    } else if (codePoint <= 0x001fffff) {
      4
    } else if (codePoint <= 0x03ffffff) {
      5
    } else { /* if (*w <= 0x7fffffff) */
      6
    }

  def encodeUtf8(ch: Int): Array[Byte] = {
    val n = getUtf8SizeWithCodePoint(ch)
    val octets = new Array[Byte](n)
    write(ch, n, octets)
    octets
  }

  def encodeUtf8(ch: Int, output: OutputStream): Unit =
    output.write(encodeUtf8(ch))

  private def getHighOctet(first: Byte, n: Int): Byte = (n match {
    case 1 => first
    case 2 => first & 0x1f
    case 3 => first & 0x0f
    case 4 => first & 0x07
    case 5 => first & 0x03
    case 6 => first & 0x01
  }).toByte

  def decodeUtf8(input: Seq[Byte]): Int =
    new ByteArrayUtf8Reader(input).nextCodePoint

  def encodeUtf16(codePoint: Int): Array[Char] = Character.toChars(codePoint)

  def isLowerCase(ch: Int): Boolean = Character.isLowerCase(ch)
  def isUpperCase(ch: Int): Boolean = Character.isUpperCase(ch)
  def isNumeric(ch: Int): Boolean = Character.isDigit(ch)
  def isAlphabet(ch: Int): Boolean = Character.isAlphabetic(ch)
  def isAlphanumeric(ch: Int): Boolean = Character.isLetterOrDigit(ch)
  def isWhiteSpace(ch: Int): Boolean = Character.isWhitespace(ch)
  def toLowerCase(ch: Int): Int = Character.toLowerCase(ch)
  def toUpperCase(ch: Int): Int = Character.toUpperCase(ch)

  def valueOfUtf8(utf8: Seq[Byte]) = new UChar(decodeUtf8(utf8))
  def valueOfUtf16(w1: Char, w2: Char) = new UChar(Character.toCodePoint(w1, w2))

  implicit def of(ch: Char): UChar = new UChar(ch)
}


class UChar private (val codePoint: Int, private val utf8: Array[Byte]) {
  import UChar._

  def this(codePoint: Int) = this(codePoint, UChar.encodeUtf8(codePoint))

  def this(utf8: Array[Byte]) =
    this(UChar.decodeUtf8(
      UChar.wrap(utf8)),
      utf8.slice(0, utf8.length))

  def isLowerCase: Boolean = Character.isLowerCase(codePoint)
  def isUpperCase: Boolean = Character.isUpperCase(codePoint)
  def isNumeric: Boolean = Character.isDigit(codePoint)
  def isAlphabet: Boolean = Character.isAlphabetic(codePoint)
  def isAlphanumeric: Boolean = Character.isLetterOrDigit(codePoint)
  def isWhiteSpace: Boolean = Character.isWhitespace(codePoint)
  def toLowerCase: UChar = new UChar(Character.toLowerCase(codePoint))
  def toUpperCase: UChar = new UChar(Character.toUpperCase(codePoint))

  def getUtf8Length: Int = utf8.length
  def toUtf8: Seq[Byte] = wrap(utf8)
  def toUtf16: Seq[Char] = wrap(encodeUtf16(codePoint))
  def printToStream(os: OutputStream): Unit = os.write(utf8)
  def appendTo(builder: StringBuilder): Unit = builder.appendAll(toUtf16)

  override def hashCode(): Int = MurmurHash3.hash32x86(utf8)
  override def toString: String = new String(encodeUtf16(codePoint))

  override def equals(other: Any): Boolean = other match {
    case that: UChar => codePoint == that.codePoint
    case _ => false
  }
}