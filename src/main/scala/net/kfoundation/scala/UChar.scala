// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}

import net.kfoundation.scala.encoding.MurmurHash3

import scala.annotation.tailrec
import scala.language.implicitConversions



object UChar {

  /**
   * Encodes input codepoints to UTF-8 and writes them to output.
   */
  trait Utf8Writer {
    private val buffer = new Array[Byte](6)

    def write(codePoint: Int): Unit = {
      val n = UChar.write(codePoint, buffer)
      writeBytes(buffer, n)
    }

    protected def writeBytes(bytes: Array[Byte], count: Int): Unit
  }


  /**
   * Writes UTF-8 encoded codepoints to the buffer allocated by the given
   * array.
   */
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


  /**
   * Writes UTF-8 encoded codepoints to the given OutputStream.
   */
  class StreamUtf8Writer(private val s: OutputStream) extends Utf8Writer {
    override protected def writeBytes(bytes: Array[Byte], count: Int): Unit =
      s.write(bytes, 0, count)
  }


  /**
   * Writes UTF-8 encoded codepoints to an expansible internal buffer. The
   * data written can be obtained by calling the get() method.
   */
  class BufferUtf8Writer private (private val s: ByteArrayOutputStream)
    extends StreamUtf8Writer(s)
  {
    def this() = this(new ByteArrayOutputStream())
    def get: Array[Byte] = s.toByteArray
  }


  /**
   * Internally encodes and stores last and only last codepoint in UTF-8.
   */
  class SingleUtf8Writer() extends Utf8Writer {
    private var value: Array[Byte] = _
    override def writeBytes(octets: Array[Byte], count: Int): Unit =
      value =  octets.slice(0, count)
    def get: Array[Byte] = value
  }


  /**
   * Reads UTF-8 encoded data into codepoints or UChar objects.
   */
  trait Utf8Reader {
    private val buffer = new Array[Byte](6)
    private var delta = 0

    def nextOctet: Int

    def nextCodePoint: Int = fillBufferAndDecode(0, -1)

    def nextUChar: Option[UChar] = {
      val ch = nextCodePoint
      if (ch >= 0) {
        Some(new UChar(ch, getLastReadOctets))
      } else
        None
    }

    def getNumberOfLastOctetsRead: Int = delta

    def getLastReadOctets: Array[Byte] = buffer.slice(0, delta)

    def writeLastReadOctetsTo(output: OutputStream): Unit =
      output.write(buffer, 0, delta)

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


  /**
   * Reads the given UTF-8 encoded array of bytes by its codepoints.
   */
  class ByteArrayUtf8Reader(private val octets: Array[Byte])
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


  /**
   * Reads the given UTF-8 encoded InputStream by its codepoints.
   */
  class StreamUtf8Reader(private val stream: InputStream) extends Utf8Reader {
    override def nextOctet: Int = stream.read()
    def mark(len: Int): Unit = stream.mark(len)
    def reset(): Unit = stream.reset()
  }


  private val NXT : Int = 0x80
  private val SEQ2: Int = 0xc0
  private val SEQ3: Int = 0xe0
  private val SEQ4: Int = 0xf0
  private val SEQ5: Int = 0xf8
  private val SEQ6: Int = 0xfc


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


  /**
   * Encodes the given codepoint to UTF-8.
   */
  def encodeUtf8(ch: Int): Array[Byte] = {
    val n = getUtf8SizeWithCodePoint(ch)
    val octets = new Array[Byte](n)
    write(ch, n, octets)
    octets
  }


  /**
   * Writes the UTF-8 encoded value of given codepoint to the give OutputStream.
   */
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


  /**
   * Converts the corresponding codepoint for the given UTF-8 encoded character.
   */
  def decodeUtf8(input: Array[Byte]): Int =
    new ByteArrayUtf8Reader(input).nextCodePoint


  /**
   * Converts the given codepoint to UTF-16 (Java native encoding).
   */
  def encodeUtf16(codePoint: Int): Array[Char] = Character.toChars(codePoint)


  /**
   * Tests if the given codepoint is a lower-case alphabetic character.
   */
  def isLowerCase(ch: Int): Boolean = Character.isLowerCase(ch)


  /**
   * Tests if the given codepoint is a upper-case alphabetic character.
   */
  def isUpperCase(ch: Int): Boolean = Character.isUpperCase(ch)


  /**
   * Tests if the given codepoint is a numeric character.
   */
  def isNumeric(ch: Int): Boolean = Character.isDigit(ch)


  /**
   * Tests if the given codepoint is an alphabetic character
   */
  def isAlphabet(ch: Int): Boolean = Character.isAlphabetic(ch)


  /**
   * Tests if the given codepoint is alphanumeric.
   */
  def isAlphanumeric(ch: Int): Boolean = Character.isLetterOrDigit(ch)


  /**
   * Tests if the given codepoint is a white space.
   */
  def isWhiteSpace(ch: Int): Boolean = Character.isWhitespace(ch)


  /**
   * Returns the codepoint corresponding to the lower-case counterpart of the
   * given codepoint.
   */
  def toLowerCase(ch: Int): Int = Character.toLowerCase(ch)


  /**
   * Returns the codepoint corresponding to the upper-case counterpart of the
   * given codepoint.
   */
  def toUpperCase(ch: Int): Int = Character.toUpperCase(ch)


  /**
   * Creates a UChar from the given raw UTF-8 encoded character.
   * @param utf8
   * @return
   */
  def valueOfUtf8(utf8: Array[Byte]) = new UChar(decodeUtf8(utf8))


  def valueOfUtf16(w1: Char, w2: Char) = new UChar(Character.toCodePoint(w1, w2))


  /**
   * Converts a native character to UChar
   */
  implicit def of(ch: Char): UChar = new UChar(ch)

}



/**
 * Represents a Unicode character. Internally, it maintains both codepoint
 * and UTF-8 representations. Conversion from native Char to UChar is
 * provided implicitly.
 *
 * <pre>
 * val ch: UChar = 'c'
 * </pre>
 */
class UChar private (val codePoint: Int, private val utf8: Array[Byte]) {
  import UChar._

  /**
   * Constructs a new UChar from codepoint representation.
   */
  def this(codePoint: Int) = this(codePoint, UChar.encodeUtf8(codePoint))


  /**
   * Constructs a new UChar from way UTF-8 representation.
   */
  def this(utf8: Array[Byte]) = this(
    UChar.decodeUtf8(utf8),
    utf8.clone())


  /**
   * Tests of this character is lowercase.
   */
  def isLowerCase: Boolean = Character.isLowerCase(codePoint)


  /**
   * Tests if this character is uppercase.
   */
  def isUpperCase: Boolean = Character.isUpperCase(codePoint)


  /**
   * Tests if this character is numeric.
   */
  def isNumeric: Boolean = Character.isDigit(codePoint)


  /**
   * Tests if this character is alphabetic.
   */
  def isAlphabet: Boolean = Character.isAlphabetic(codePoint)


  /**
   * Tests if this character is alphanumeric.
   */
  def isAlphanumeric: Boolean = Character.isLetterOrDigit(codePoint)


  /**
   * Tests if this character is a white space.
   */
  def isWhiteSpace: Boolean = Character.isWhitespace(codePoint)


  /**
   * Converts this character to lowercase.
   */
  def toLowerCase: UChar = new UChar(Character.toLowerCase(codePoint))


  /**
   * Converts this character to uppercase.
   */
  def toUpperCase: UChar = new UChar(Character.toUpperCase(codePoint))


  /**
   * Returns the number of bytes used for UTF-8 representation of this
   * character.
   */
  def getUtf8Length: Int = utf8.length


  /**
   * Returns UTF-8 representation of this character.
   */
  def toUtf8: Array[Byte] = utf8


  /**
   * Computes and returns UTF-16 representation of this character.
   */
  def toUtf16: Array[Char] = encodeUtf16(codePoint)


  /**
   * Writes this character to the given output stream in UTF-8.
   */
  def writeToStream(os: OutputStream): Unit = os.write(utf8)


  /**
   * Appends this character to the given StringBuilder (after converting to
   * native UTF-16).
   */
  def appendTo(builder: StringBuilder): Unit = builder.appendAll(toUtf16)


  override def hashCode(): Int = MurmurHash3.hash32x86(utf8)


  override def toString: String = new String(encodeUtf16(codePoint))


  override def equals(other: Any): Boolean = other match {
    case that: UChar => codePoint == that.codePoint
    case _ => false
  }

}