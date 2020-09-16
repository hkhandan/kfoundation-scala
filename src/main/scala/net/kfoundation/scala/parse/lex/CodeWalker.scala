// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse.lex

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}

import net.kfoundation.scala.io.Path
import net.kfoundation.scala.parse.{CodeLocation, CodeRange, MutableCodeLocation}
import net.kfoundation.scala.{UChar, UString}

import scala.annotation.tailrec



/**
 * Factory for [[CodeWalker]] instances.
 */
object CodeWalker {
  type PatternCheckFunction = (Int, Boolean) => Boolean

  private val LF: Byte = 13
  private val CR: Byte = 10
  private val SPACE: Byte = 32


  /**
   * Produces a CodeWalker to parse the given string.
   */
  def of(str: UString) = new CodeWalker("$buffer",
    new ByteArrayInputStream(str.toUtf8))


  /**
   * Creates a CodeWalker to process the given input stream.
   */
  def of(input: InputStream) = new CodeWalker("$stream", input)


  /**
   * Produces a CodeWalker for reading file pointed by the given path.
   */
  def of(path: Path) = new CodeWalker(
    path.getFileName
      .getOrElse("<file>"),
    path.getInputStream)
}



/**
 * Facilitates parsing (lexing) of a UTF-8 encoded stream. All parsers and
 * deserializers in KFoundation are created using CodeWalker. They can serve
 * as good examples for how to use this class. Usage of CodeWalker
 * often involves one or several calls to tryRead(), read(), or readAll()
 * methods, followed by a call to commit().
 *
 * For performance and streamlining reasons, this classes is designed with
 * there-is-no-going-back policy. Meaning that, when a call to tryRead() succeeds,
 * it is not possible to reset the stream to an earlier point, i.e. there is
 * no rollback() method. If tryRead() fails the stream position will remain
 * where it was before it was called.
 *
 * For each successful call to a read method, the data read is appended to an
 * internal buffer. This can be retrieved using getCurrentSelection() method.
 *
 * The job of commit() method is to reset the internal buffer and return the
 * input code range representing the location of data in that buffer
 * within the input data.
 *
 * One can use skipAll() methods to discard a portion of stream matching a given
 * criteria. skipSpaces() is a specialization of skipAll() that skips over
 * spaces and newline characters. Beware that a call to these methods empties
 * the internal buffer.
 *
 * To test for end-of-stream use hasMore() method.
 *
 * @param inputName such as file name; this is used mainly for error messages.
 * @param input the stream to be processes
 */
class CodeWalker(inputName: String, input: InputStream) {
  import CodeWalker._

  private val reader = new UChar.StreamUtf8Reader(input)
  private var begin = new CodeLocation(inputName)
  private val end = new MutableCodeLocation(inputName)
  private val buffer = new ByteArrayOutputStream()


  private def step(): Unit = {
    end.step(reader.getNumberOfLastOctetsRead)
    reader.writeLastReadOctetsTo(buffer)
  }


  private def step(b: Byte): Unit = {
    end.step(1)
    buffer.write(b)
  }


  private def step(bytes: Array[Byte], nChars: Int): Unit = {
    end.step(nChars, bytes.length)
    buffer.write(bytes, 0, bytes.length)
  }


  /**
   * Discards the internal buffer, returning the code range representing the
   * location of that buffer within the input data.
   */
  def commit(): CodeRange = {
    val b = begin
    begin = end.immutableCopy
    buffer.reset()
    new CodeRange(b, begin)
  }


  /**
   * Returns the data read since the last call to commit(), or if not called,
   * since the beginning of the input stream.
   */
  def getCurrentSelection: UString = UString.of(buffer.toByteArray)


  /**
   * The lower bound of internal buffer. This is where commit() was last
   * called, or the beginning of stream.
   */
  def getBegin: CodeLocation = begin


  /**
   * Location next to the last successful read.
   */
  def getCurrentLocation: CodeLocation = end.immutableCopy


  /**
   * Test if the input stream has reached its end.
   */
  def hasMore: Boolean = input.available() > 0


  /**
   * Facility method to produce an error containing the file name and start
   * location of the buffer.
   */
  def lexicalErrorAtBeginning(message: String): LexicalError =
    new LexicalError(begin, message)


  /**
   * Facility method to produce an error containing file name and location
   * currently being read.
   */
  def lexicalErrorAtCurrentLocation(message: String): LexicalError =
    new LexicalError(getCurrentLocation, message)


  private def tryRead(b: Byte): Boolean = {
    reader.mark(1)
    if(reader.nextOctet == b) {
      step(b)
      true
    } else {
      reader.reset()
      false
    }
  }


  private def tryRead(bytes: Array[Byte], nChars: () => Int): Boolean = {
    @tailrec
    def tryReadNext(bytes: Array[Byte], i: Int): Boolean =
      if(i == bytes.length) {
        true
      } else if(reader.nextOctet == bytes(i)) {
        tryReadNext(bytes, i + 1)
      } else {
        false
      }

    reader.mark(bytes.length)

    if(tryReadNext(bytes, 0)) {
      step(bytes, nChars())
      true
    } else {
      reader.reset()
      false
    }
  }


  /** Test for and read a unicode character. */
  def tryRead(ch: UChar): Boolean = tryRead(ch.toUtf8, () => 1)


  /** Test for and read a unicode string. */
  def tryRead(str: UString): Boolean = tryRead(str.toUtf8, () => str.getLength)


  /** Test for and read a space, carriage-return, or line-feed character. */
  def tryReadWhiteSpace: Boolean =
    if(tryRead(SPACE)) {
      true
    } else if(tryRead(CR)) {
      tryRead(LF)
      end.newLine
      true
    } else if(tryRead(LF)) {
      end.newLine
      true
    } else {
      false
    }


  /** Read all following spaces, keeping them in internal buffer. */
  def readSpaces(): Unit = {
    while(tryReadWhiteSpace) {}
  }


  /** Read and discards all following spaces and anything in internal buffer. */
  final def skipSpaces(): Unit = {
    readSpaces()
    commit()
  }

  /**
   * With input being UTF8-encoded, reads one unicode codepoint satisfying the
   * given criteria, and returns its value if succeeds. Otherwise, returns -1.
   */
  def tryRead(test: Int => Boolean): Int = {
    reader.mark(8)
    val ch = reader.nextCodePoint
    if(test(ch)) {
      step()
      ch
    } else {
      reader.reset()
      -1
    }
  }


  /**
   * Test and read a numeric character, returning its numeric value.
   */
  def tryReadDigit: Int = {
    reader.mark(8)
    val ch = reader.nextCodePoint
    if(UChar.isNumeric(ch)) {
      step()
      ch - '0'
    } else {
      reader.reset()
      -1
    }
  }


  /**
   * Read all following sequential occurrences of the given character.
   */
  def readAll(ch: UChar): Int = {
    var n = 0
    while(tryRead(ch)) {
      n += 1
    }
    n
  }


  /**
   * With input being UTF8-encoded, reads all following unicode codepoints
   * satisfying the given criteria, and returns the number of codepoints read
   * (0 if none).
   */
  def readAll(test: Int => Boolean): Int = {
    var n = 0
    while(tryRead(test) >= 0) {
      n += 1
    }
    n
  }


  /**
   * Discards the next codepoint in input data if it satisfies the given
   * criteria, discarding the internal buffer as well.
   */
  def skip(test: Int => Boolean): Boolean = {
    reader.mark(8)
    val ch = reader.nextCodePoint
    if(test(ch)) {
      if(ch == CR) {
        end.newLine
      } else {
        end.step(1)
      }
      true
    } else {
      reader.reset()
      false
    }
  }


  /**
   * Discards all following codepoints that satisfy the given
   * criteria, discarding the internal buffer as well.
   */
  def skipAll(test: Int => Boolean): Int = {
    var n = 0
    while(skip(test)) {
      n += 1
    }
    commit()
    n
  }


  /**
   * Reads any character. Returns None if there is nothing to read.
   */
  def tryReadUChar: Option[UChar] = {
    reader.mark(8)
    val ch = reader.nextUChar
    if(ch.isDefined) {
      step()
    } else {
      reader.reset()
    }
    ch
  }


  override def toString: String = "End: " + `end`.toString

}
