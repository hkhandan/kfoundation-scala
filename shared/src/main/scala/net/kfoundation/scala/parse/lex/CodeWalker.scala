// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse.lex

import net.kfoundation.scala.io.Path
import net.kfoundation.scala.parse._
import net.kfoundation.scala.{UChar, UString}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import scala.annotation.tailrec



/**
 * Factory for [[CodeWalker]] instances.
 */
object CodeWalker {
  trait PatternWalker {
    def read(b: Int): PatternWalker
    def read(predicate: Int => Boolean): PatternWalker
    def readAll(predicate: Int => Boolean): PatternWalker
    def read(ch: UChar): PatternWalker
    def read(str: UString): PatternWalker
    def test(b: Int): PatternWalker
    def test(predicate: Int => Boolean): PatternWalker
    def test(ch: UChar): PatternWalker
    def test(str: UString): PatternWalker
    def get: Option[UString]
  }

  private class PatternWalkerImpl(reader: UChar.StreamUtf8Reader,
      begin: CodeLocation, commit: CodeLocation => ())
    extends PatternWalker
  {
    private val buffer = new ByteArrayOutputStream()
    private var failed: Boolean = false
    private var recoverByte: Int = -1
    private val end: MutableCodeLocation = begin

    private def step(b: Int): Unit = {
      buffer.write(b)
      end.step(1)
    }

    private def testStep(): Unit = end.step(1)

    private def read(): Int = {
      if(recoverByte == -2) {
        throw new IllegalStateException("PatternWalker can be used only once")
      } else if(recoverByte == -1) {
        reader.nextOctet
      } else {
        val b = recoverByte
        recoverByte = -1
        b
      }
    }

    private def fail(): Unit = {
      failed = true
      reader.reset()
    }

    override def read(b: Int): PatternWalker = {
      if(!failed) {
        if(read() != b) {
          fail()
        } else {
          step(b)
        }
      }
      this
    }

    override def read(predicate: Int => Boolean): PatternWalker = {
      val b = read()
      if(!failed) {
        if(!predicate(b)) {
          fail()
        } else {
          step(b)
        }
      }
      this
    }

    override def readAll(predicate: Int => Boolean): PatternWalker = {
      var b = read()
      while(b != -1 && predicate(b)) {
        step(b)
        b = read()
      }
      recoverByte = b
      this
    }

    private def read(bytes: Array[Byte]): PatternWalker = {
      @tailrec
      def tryReadNext(i: Int, b: Int): Boolean =
        if(b == bytes(i)) {
          step(b)
          if (i == bytes.length) {
            true
          } else {
            tryReadNext(i + 1, read())
          }
        } else {
          false
        }
      if(!failed && !tryReadNext(0, read())) {
        fail()
      }
      this
    }

    override def read(ch: UChar): PatternWalker = read(ch.toUtf8)

    override def read(str: UString): PatternWalker = read(str.toUtf8)

    override def test(b: Int): PatternWalker = {
      if(!failed) {
        if(read() != b) {
          fail()
        } else {
          testStep()
        }
      }
      this
    }

    override def test(predicate: Int => Boolean): PatternWalker = {
      if(!failed) {
        if(!predicate(read())) {
          fail()
        } else {
          testStep()
        }
      }
      this
    }

    private def test(bytes: Array[Byte]): PatternWalker = {
      @tailrec
      def tryReadNext(i: Int, b: Int): Boolean =
        if(b == bytes(i)) {
          testStep()
          if (i == bytes.length) {
            true
          } else {
            tryReadNext(i + b, read())
          }
        } else {
          false
        }
      if(!failed && !tryReadNext(0, read())) {
        fail()
      }
      this
    }

    override def test(ch: UChar): PatternWalker = test(ch.toUtf8)

    override def test(str: UString): PatternWalker = test(str.toUtf8)

    override def get: Option[UString] = {
      recoverByte = -2
      if(failed)
        None
      else {
        commit(end.immutableCopy)
        Some(UString.of(buffer.toByteArray))
      }
    }
  }


  type PatternCheckFunction = (Int, Boolean) => Boolean

  private val LF: Byte = 13
  private val CR: Byte = 10
  private val SPACE: Byte = 32
  private val ROW: UString = "row"
  private val COL: UString = "col"
  val NOT_FOUND: Int = -1


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
    path.fileName.map(_.toString)
      .getOrElse("<file>"),
    path.newInputStream)
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


  def parseError(key: UString,
      params: (UString, UString)*): ParseError =
    new ParseError(key, params :+
      (ROW, UString.of(end.getRow)) :+
      (COL, UString.of(end.getCol)) :_*)


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
   * given criteria, and returns its value if succeeds. Otherwise, returns
   * NOT_FOUND (-1).
   */
  def tryRead(test: Int => Boolean): Int = {
    reader.mark(8)
    val ch = reader.nextCodePoint
    if(test(ch)) {
      step()
      ch
    } else {
      reader.reset()
      NOT_FOUND
    }
  }

  def tryRead(codePoint: Int): Boolean = {
    reader.mark(8)
    val ch = reader.nextCodePoint
    if(ch == codePoint) {
      step()
      true
    } else {
      reader.reset()
      false
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
      NOT_FOUND
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


  def patternWalker(ahead: Int): PatternWalker = {
    reader.mark(ahead)
    new PatternWalkerImpl(reader, end.immutableCopy, e => {
      end.set(e)
      begin = e
    })
  }


  override def toString: String = "End: " + `end`.toString
}
