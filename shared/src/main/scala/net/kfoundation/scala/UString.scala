// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala

import net.kfoundation.scala.UChar._
import net.kfoundation.scala.UString.{CR, NOT_FOUND, PIPE, builder}
import net.kfoundation.scala.encoding.{DecodingException, MurmurHash3}

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
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


  /** High-performance buffered builder for UStrings */
  class Builder {
    private val buffer = new ByteArrayOutputStream()

    /** Appends a UString to the buffer. */
    def append(str: UString): Builder = {
      buffer.write(str.toUtf8)
      this
    }

    /** Appends a String to the buffer. */
    def append(str: String): Builder = append(UString.of(str))

    /** Appends an Int to the buffer. */
    def append(n: Int): Builder = append(UString.of(n))

    /** Appends a Long to the buffer. */
    def append(n: Long): Builder = append(UString.of(n))

    /** Appends a Double to the buffer. */
    def append(n: Double): Builder = append(UString.of(n))

    /**
     * Replaces all occurances of `ch` in `str` with `replacement` and
     * appends the result to the buffer.
     */
    def appendReplacing(str: UString, ch: UChar, replacement: UString): Builder = {
      str.uCharIterator.foreach(c =>
        if(c.equals(ch)) append(replacement)
        else append(c))
      this
    }

    /** Appends a Char to the buffer. */
    def append(ch: Char): Builder = {
      buffer.write(UChar.encodeUtf8(ch))
      this
    }

    /** Appends a UChar to the buffer. */
    def append(ch: UChar): Builder = {
      buffer.write(ch.toUtf8)
      this
    }

    /** Appends a UObject to the buffer. */
    def append(obj: UObject): Builder = {
      obj.appendTo(this)
      this
    }

    private def digitToHex(b: Int): Int = if(b < 10) b + 48 else b + 55

    private def appendHexByte(b: Int): Unit = {
      append(digitToHex((b >> 4) & 0xF).toChar)
      append(digitToHex(b & 0xF).toChar)
    }

    /**
     * Appends the hexadecimal representation of the given byte array to the
     * buffer.
     */
    def appendHex(bytes: Array[Byte]): Builder = {
      bytes.foreach(appendHexByte(_))
      this
    }

    /**
     * Appends the hexadecimal representation of the given byte to the buffer.
     */
    def appendHex(n: Byte): Builder = {
      appendHexByte(n)
      this
    }

    /**
     * Appends the hexadecimal representation of the given number to the buffer.
     */
    def appendHex(n: Long): Builder = {
      val buffer = ByteBuffer.allocate(8)
      buffer.putLong(n)
      appendHex(buffer.array())
    }

    private def appendJoining(items: Iterable[UObject], delimiter: Array[Byte]): Builder = {
      val it = items.iterator
      while(it.hasNext) {
        it.next().appendTo(this)
        if(it.hasNext) {
          buffer.write(delimiter)
        }
      }
      this
    }

    /**
     * Appends all given items to the buffer, joining them using the given
     * delimiter.
     */
    def appendJoining(items: Iterable[UObject], delimiter: UChar): Builder =
      appendJoining(items, delimiter.toUtf8)

    /**
     * Appends all given items to the buffer, joining them using the given
     * delimiter.
     */
    def appendJoining(items: Iterable[UObject], delimiter: UString): Builder =
      appendJoining(items, delimiter.toUtf8)

    /**
     * Appends all the given items to the buffer. For items that are not
     * UObject, their toString() method will be used.
     */
    def appendAll(items: Any*): Builder = {
      items.foreach(_ match {
        case str: UString => append(str)
        case obj: UObject => obj.appendTo(this)
        case ch: Char => append(ch)
        case any => append(any.toString)
      })
      this
    }

    /**
     * Iterates the given items and calls `fn` for each element to let the
     * user code interpret and append that item to the buffer, while
     * putting the given delimiter in between.
     */
    def unfold[T](items: Seq[T], delimiter: UString,
      fn: (Builder, T) => ()): Builder =
    {
      val it = items.iterator
      while(it.hasNext) {
        fn(this, it.next())
        if(it.hasNext) {
          buffer.write(delimiter.toUtf8)
        }
      }
      this
    }

    /** Convenience method for fluent notation.  */
    def use(fn: Builder => Unit): Builder = {
      fn(this)
      this
    }

    /** Returns the internal buffer as an UString. */
    def build: UString = new UString(buffer.toByteArray)

    /** The size of the string currently held in the buffer. */
    def size: Int = buffer.size()
  }


  /**
   * Provides U"..." notation for creating UStrings.
   */
  implicit class Interpolator(ctx: StringContext) {
    def U(expr: Any*): UString = {
      val builder = new Builder
      val partsIt = ctx.parts.iterator
      val exprIt = expr.iterator
      while(exprIt.hasNext) {
        builder.append(partsIt.next())
        exprIt.next() match {
          case u: UObject => u.appendTo(builder)
          case ch: Char => builder.append(ch)
          case any => builder.append(any.toString)
        }
      }
      builder.append(partsIt.next()).build
    }
  }


  implicit class IntWrapper(value: Int) {
    def toUString: UString = of(value)
  }


  implicit class LongWrapper(value: Long) {
    def toUString: UString = of(value)
  }


  implicit class DoubleWrapper(value: Double) {
    def toUString: UString = of(value)
  }


  private val NULL = new UString("null")
  private val PIPE: Byte = '|'
  private val CR: Byte = '\n'

  /** Returned by find() method on failure */
  val NOT_FOUND: Int = -1

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


  implicit def of(str: UString): String = str.toString


  /**
   * Converts a native string to UString.
   */
  implicit def of(str: String): UString = new UString(str)


  /**
   * Converts a UChar to UString.
   */
  def of(ch: UChar): UString = ch.toUString


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


  /** Produces the hexadecimal representation of the given byte array. */
  def ofHex(bytes: Array[Byte]): UString =
    builder.appendHex(bytes).build


  /** Produces the hexadecimal representation of the given number. */
  def ofHex(n: Long): UString = builder.appendHex(n).build


  private def join(objSeq: Iterable[UObject], delimiter: Array[Byte]): UString = {
    val seq = objSeq.map(_.toUString)
    val size = seq.foldLeft(0)((a, b) => a + b.getUtf8Length) +
      delimiter.length*Math.max(seq.size-1, 0)
    val output = new ByteArrayOutputStream(size)
    val it = seq.iterator
    while(it.hasNext) {
      output.write(it.next().getOctets)
      if(it.hasNext) {
        output.write(delimiter)
      }
    }
    new UString(output.toByteArray)
  }


  /**
   * Produces a UString joining the given array UStrings, putting the given
   * delimiter in between them.
   */
  def join(parts: Iterable[UObject], delimiter: UString): UString =
    join(parts, delimiter.toUtf8)


  /**
   * Joins the given list of objects into a UString with putting the given
   * delimiter in between them.
   */
  def join(parts: Iterable[UObject], delimiter: UChar): UString =
    join(parts, delimiter.toUtf8)


  /** Joins the given objects into a UString. */
  def join(parts: UObject*): UString = join(parts, Array.emptyByteArray)


  /** Create a new Builder instance. */
  def builder: Builder = new Builder
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
 * can be done directly over the UTF-8 stream. By avoiding unnecessary encoding and
 * decoding to and from UTF-8, unlike Java String, we can save our CPU
 * resources. Also since UTF-8 is the most compact Unicode representation, we save
 * some memory as well.
 *
 * Use toUtf8() to get raw bytes consisting this string, or if necessary use
 * uCharIterator() to read each character one-by-one.
 *
 * getLength() returns the length of string in characters rather than bytes. To
 * get the number of bytes use getUtf8Length(). substring() methods also
 * work based on character (codepoint) location, as one would naturally expect.
 *
 * @constructor creates a UString from a UTF-8 encoded raw array of bytes.
 */
class UString private(octets: Array[Byte]) extends UObject {
  private var length: Integer = _


  /** Creates a UString from a native String */
  def this(nativeString: String) = this(
    nativeString.getBytes(StandardCharsets.UTF_8))


  private def getOctets: Array[Byte] = octets


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
      NOT_FOUND
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


  def contains(char: UChar): Boolean = find(char, 0) != NOT_FOUND


  def contains(str: UString): Boolean = find(str, 0) != NOT_FOUND


  /**
   * Uses the given mapping function to convert characters of this string, and
   * returns the resulting string.
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


  private def subSequence(begin: Int, end: Int): UString = {
    val size = end - begin
    val result = new Array[Byte](size)
    Array.copy(octets, begin, result, 0, size)
    new UString(result)
  }


  /**
   * Creates a copy of the portion of this string starting (inclusive) and
   * ending (exclusive) at the given values.
   */
  def subString(begin: Int, end: Int): UString = {
    val l1 = locationOfCodePointAtIndex(0, 0, begin)
    val l2 = locationOfCodePointAtIndex(l1, begin, end)
    subSequence(l1, l2)
  }


  /**
   * Creates a copy of the portion of this string starting (inclusive) at the
   * given location up to the end.
   */
  def subString(begin: Int): UString = {
    val l = locationOfCodePointAtIndex(0, 0, begin)
    subSequence(l, octets.length)
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


  def toInt: Int = toString.toInt


  def toInt(radix: Int): Int = Integer.parseInt(toString, radix)


  /**
   * Parses this string to a Long
   */
  def toLong: Long =  toString.toLong


  def toLong(radix: Int): Long = java.lang.Long.parseLong(toString, radix)


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


  @tailrec
  private def split(ch: UChar, offset: Int, s: Seq[UString]): Seq[UString] = {
    val pos = find(ch, offset)
    if(pos == NOT_FOUND) {
      s.:+(subSequence(offset, octets.length))
    } else {
      split(ch, pos + ch.getUtf8Length, s.:+(subSequence(offset, pos)))
    }
  }


  def split(ch: UChar): Seq[UString] = split(ch, 0, Seq.empty)


  def replace(ch: UChar, replacement: UString): UString =
    builder.appendReplacing(this, ch, replacement).build


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


  override def appendTo(builder: UString.Builder): Unit = builder.append(this)


  override def toUString: UString = this
}