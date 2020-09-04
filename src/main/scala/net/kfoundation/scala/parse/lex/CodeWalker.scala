package net.kfoundation.scala.parse.lex

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}

import net.kfoundation.scala.io.Path
import net.kfoundation.scala.parse.{CodeLocation, CodeRange, MutableCodeLocation}
import net.kfoundation.scala.{UChar, UString}

import scala.annotation.tailrec



object CodeWalker {
  type PatternCheckFunction = (Int, Boolean) => Boolean

  private val LF: Byte = 13
  private val CR: Byte = 10
  private val SPACE: Byte = 32

  def load(path: Path): CodeWalker =
    new CodeWalker(
      path.toString,
      path.getInputStream)

  def of(str: UString) = new CodeWalker("buffer",
    new ByteArrayInputStream(str.toUtf8))
}


class CodeWalker(inputName: String, input: InputStream)
{
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


  def commit(): CodeRange = {
    val b = begin
    begin = end.immutableCopy
    buffer.reset()
    new CodeRange(inputName, b, begin)
  }


  def rollback(): Unit = {
    end.set(begin)
    buffer.reset()
  }


  def getCurrentSelection: UString = UString.of(buffer.toByteArray)


  def getBegin: CodeLocation = begin


  def getCurrentLocation: CodeLocation = end.immutableCopy


  def hasMore: Boolean = input.available() > 0


  def lexicalErrorAtBeginning(message: String): LexicalError =
    new LexicalError(begin, message)


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


  def tryRead(ch: UChar): Boolean = tryRead(ch.toUtf8, () => 1)


  def tryRead(str: UString): Boolean = tryRead(str.toUtf8, () => str.getLength)


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


  def readSpaces(): Unit = {
    while(tryReadWhiteSpace) {}
  }


  final def skipSpaces(): Unit = {
    readSpaces()
    commit()
  }


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


  def tryReadAll(ch: UChar): Int = {
    var n = 0
    while(tryRead(ch)) {
      n += 1
    }
    n
  }


  def tryReadAll(test: Int => Boolean): Int = {
    var n = 0
    while(tryRead(test) >= 0) {
      n += 1
    }
    n
  }


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
}
