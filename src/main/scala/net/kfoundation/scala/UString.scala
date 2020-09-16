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
import java.nio.charset.StandardCharsets

import net.kfoundation.scala.UChar._
import net.kfoundation.scala.UString.{CR, PIPE}
import net.kfoundation.scala.encoding.{DecodingException, MurmurHash3}

import scala.annotation.tailrec
import scala.language.implicitConversions



object UString {

  private class CodePointIterator(private val octets: Array[Byte]) extends Iterator[Int] {
    private val reader = new ByteArrayUtf8Reader(octets)
    private var ch = reader.nextCodePoint

    override def hasNext: Boolean = ch != -1

    override def next(): Int = {
      val v = ch
      ch = reader.nextCodePoint
      v
    }
  }


  private class UCharIterator(private val octets: Array[Byte]) extends Iterator[UChar] {
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


  /**
   * Provides U"..." notation for creating UStrings.
   */
  implicit class UStringInterpolation(ctx: StringContext) {
    object U {
      def apply(expr: Any*): UString = new UString(ctx.s(expr:_*))
    }
  }


  private val NULL = new UString("null")
  private val PIPE: Byte = '|'
  private val CR: Byte = '\n'

  /** Empty UString */
  val EMPTY: UString = new UString(Array.empty[Byte])


  private def validateUtf8(bytes: Array[Byte]): Boolean = {
    val n = bytes.length
    var i = 0
    while(i < n) {
      i += getUtf8SizeWithFirstOctet(bytes(i))
    }
    i == n
  }

  /** Reads a UString from the UTF-8 encoded stream. */
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


  /**
   * Converts a native string to UString.
   */
  implicit def of(str: String): UString = new UString(str)


  /**
   * Converts a UChar to UString.
   */
  def of(ch: UChar): UString = new UString(ch.toUtf8)


  /**
   * Converts a raw array of UTF-8 encoded bytes to UString.
   */
  def of(octets: Array[Byte]) = new UString(octets)


  /**
   * Converts a way sequence of UTF-8 encoded bytes to UString.
   */
  def of(octets: Seq[Byte]) = new UString(octets.toArray)


  /**
   * Converts the given portion of a way UTF-8 encoded byte array to UString.
   */
  def of(octets: Array[Byte], offset: Int, size: Int): UString = {
    val selection = new Array[Byte](size)
    octets.drop(offset).copyToArray(selection)
    new UString(selection)
  }


  /**
   * Coverts a Long value to UString
   */
  def of(n: Long): UString = of(n.toString)


  /**
   * Converts a Double value to UString
   */
  def of(n: Double): UString = of(n.toString)


  /**
   * Produces a UString joining the given array UStrings, putting the given
   * delimiter in between them.
   */
  def join(seq: Seq[UString], delimiter: UString): UString = {
    val size = seq.foldLeft(0)((a, b) => a + b.getUtf8Length) +
      delimiter.getUtf8Length*Math.max(seq.length-1, 0)
    val output = new ByteArrayOutputStream(size)
    val it = seq.iterator
    while(it.hasNext) {
      output.write(it.next().getOctets)
      if(it.hasNext) {
        output.write(delimiter.getOctets)
      }
    }
    new UString(output.toByteArray)
  }

}



/**
 * High-performance string with internal UTF-8 encoding. Conveniently use
 * interpolator U to create instances of UString.
 *
 * <pre>
 * val myString = U"This is my string"  // type of myString is UString
 * </pre>
 *
 * Conversion from native String to UString is provided implicitly.
 *
 * <pre>
 * val myString: UString = "This is my string"
 * </pre>
 *
 * The philosophy behind this class is that, more often than not, text data is
 * stored and read in UTF-8 encoding, and written to output with little or no
 * processing. Most often such process can be done at byte level (such as
 * concatenation), and in cases when iteration of code points is required, it
 * can be done directly over UTF-8 stream. By avoiding unnecessary encoding and
 * decoding to and from UTF-8, unlike Java'S String, we can save our CPU
 * resources. Also since UTF-8 is most compact Unicode representation, we save
 * some memory as well.
 *
 * Use toUtf8() to get raw bytes consisting this string, or if necessary use
 * uCharIterator() to read each character.
 *
 * getLength() returns the length of string in characters rather than bytes. To
 * get the number of bytes use getUtf8Length(). substring() methods also
 * work based on character (codepoint) location, as one would naturally expect.
 *
 * @constructor creates a UString from a UTF-8 encoded raw array of bytes.
 */
class UString private(octets: Array[Byte]) {
  private var length: Integer = null


  /** Creates a UString from a native String */
  def this(nativeString: String) = this(
    nativeString.getBytes(StandardCharsets.UTF_8))


  private def getOctets = octets


  private def codePointIterator: Iterator[Int] =
    new UString.CodePointIterator(octets)


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
    if(i < 0) {
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


  /**
   * Returns the raw UTF-8 representation of this string.
   */
  def toUtf8: Array[Byte] = octets


  /**
   * Returns an iterator to traverse this string by character (codepoint).
   */
  def uCharIterator: Iterator[UChar] = new UString.UCharIterator(octets)


  /**
   * Returns the number of characters (codepoints) in this string. This value
   * is computed and cached the first time this method is called.
   */
  def getLength: Int = {
    if(length == null) {
      length = getCharCount(octets.length, 0, 0)
    }
    length
  }

  /**
   * Returns the number of bytes in UTF-8 representation of this string.
   */
  def getUtf8Length: Int = octets.length


  /**
   * Tests if this string is empty
   */
  def isEmpty: Boolean = octets.isEmpty


  /**
   * Returns the octet at the given index of UTF-8 representation of this
   * string.
   */
  def getOctetAt(i: Int): Byte = octets(i)


  /**
   * Compares two strings ignoring cases.
   */
  def equalsIgnoreCase(that: UString): Boolean = equalsIgnoreCase(
    new ByteArrayUtf8Reader(this.octets),
    new ByteArrayUtf8Reader(that.toUtf8))


  /**
   * Finds the index of the first occurrence of the given byte after the given
   * offset in the UTF-8 representation of this string.
   */
  @tailrec
  final def find(octet: Byte, offset: Int): Int =
    if(offset >= octets.length) {
      -1
    } else if(octets(offset) == octet) {
      offset
    } else {
      find(octet, offset + 1)
    }


  /**
   * Finds the index of the first occurrence of the given byte after the given
   * offset in the UTF-8 representation of this string.
   */
  def find(char: UChar, offset: Int): Int = find(char.toUtf8, offset)


  /**
   * Finds the index of the first occurrence of the given byte after the given
   * offset in the UTF-8 representation of this string.
   */
  def find(str: UString, offset: Int): Int = find(str.getOctets, offset)


  /**
   * Uses the given mapping function to convert characters of this string, and
   * returns the resuling string.
   */
  def mapCodePoints(fn: Int => Int): UString = new UString(
    codePointIterator.map(fn)
      .foldLeft(
        new BufferUtf8Writer())(
        (w, ch) => {w.write(ch); w})
      .get)


  /**
   * Converts this string to lower-case.
   */
  def toLowerCase: UString = mapCodePoints(UChar.toLowerCase)


  /**
   * Converts this string to upper-case.
   */
  def toUpperCase: UString = mapCodePoints(UChar.toUpperCase)


  /**
   * Converts only the first letter of this string to uppercase.
   * @return
   */
  def toFirstUpperCase: UString = {
    val writer = new BufferUtf8Writer()
    val it = codePointIterator
    writer.write(UChar.toUpperCase(it.next()))
    it.foreach(ch => writer.write(ch))
    new UString(writer.get)
  }


  /**
   * Creates a copy of the portion of this string starting (inclusive) and
   * ending (exclusive) at the given values.
   */
  def subString(begin: Int, end: Int): UString = {
    val l1 = locationOfCodePointAtIndex(0, 0, begin)
    val l2 = locationOfCodePointAtIndex(l1, begin, end)
    val size = l2 - l1
    val result = new Array[Byte](size)
    Array.copy(octets, l1, result, 0, size)
    new UString(result)
  }


  /**
   * Creates a copy of the portion of this string starting (inclusive) at the
   * given location up to the end.
   */
  def subString(begin: Int): UString = {
    val l = locationOfCodePointAtIndex(0, 0, begin)
    val size = octets.length - l
    val result = new Array[Byte](size)
    Array.copy(octets, l, result, 0, size)
    new UString(result)
  }


  /**
   * Produces a new string appending the one given to the end of this string.
   */
  def append(str: UString): UString = append(str.getOctets)


  /**
   * Produces a new string appending a raw UTF-8 encoded string to the end of
   * this one.
   */
  def append(raw: Array[Byte]): UString = new UString(octets ++ raw)


  /**
   * Produces a new string appending the given unicode character to the end of
   * this string.
   */
  def append(ch: UChar): UString = append(ch.toUtf8)


  /**
   * Writes this string to the given OutputString in its UTF-8 encoded form.
   */
  def writeToStream(os: OutputStream): Unit = os.write(octets)


  /**
   * Appends two UString objects to produce a new one.
   */
  def +(v: UString): UString = append(v)


  /**
   * Appends a UString object to any other object using its toString() method.
   */
  def +(o: Any): UString = append(
    if(o == null) UString.NULL else new UString(o.toString))


  /**
   * Parses this string to a Long
   */
  def toLong: Long =  toString.toLong


  /**
   * Parses this string to a Double
   */
  def toDouble: Double = toString.toDouble


  /**
   * Similar to StringOps.stripMargin, used to facilitate creating multiline
   * strings with U interpolator. It strips '|' character and every space
   * preceding it at the beginning of each line.
   */
  def stripMargin: UString = {
    val buffer = new ByteArrayOutputStream()
    var ignore = false
    octets.foreach(o => {
      if(o == PIPE) {
        ignore = false
      } else {
        if(!ignore) {
          buffer.write(o)
        }
        if(o == CR) {
          ignore = true
        }
      }
    })
    new UString(buffer.toByteArray)
  }


  /**
   * Converts this string to corresponding native representation.
   */
  override def toString: String = new String(octets, StandardCharsets.UTF_8)


  /**
   * Computes and returns the hashcode for this string using MurmurHash.
   */
  override def hashCode(): Int = MurmurHash3.hash32x86(octets)


  override def equals(other: Any): Boolean = other match {
    case that: UString => (this eq that) ||
      (this.octets.length == that.getUtf8Length &&
        octetsEqual(octets.length - 1, that.getOctets))
    case _ => false
  }

}