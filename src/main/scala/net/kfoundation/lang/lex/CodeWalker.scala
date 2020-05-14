package net.kfoundation.lang.lex

import java.nio.file.{Files, Path}

import net.kfoundation.lang.{CodeLocation, CodeRange, MutableCodeLocation}
import net.kfoundation.{UChar, UString}

import scala.annotation.tailrec


object CodeWalker {
  type PatternCheckFunction = (Int, Boolean) => Boolean

  private val LF: Byte = 13
  private val CR: Byte = 10
  private val SPACE: Byte = 32

  def load(path: Path): CodeWalker =
    new CodeWalker(
      path.toString,
      Files.readAllBytes(path))

  def of(str: UString) = new CodeWalker("UString", str.toUtf8)
}


class CodeWalker private (file: String, private val octets: Seq[Byte])
{
  import CodeWalker._

  private var begin = new CodeLocation()
  private val end = new MutableCodeLocation()
  private val reader = new UChar.ByteArrayUtf8Reader(octets)

  @tailrec
  private def tryRead(seq: Seq[Byte], i: Int): Boolean =
    if(i == seq.length) {
      true
    } else if(reader.nextOctet == seq(i)) {
      tryRead(seq, i + 1)
    } else {
      reader.seek(end.getStreamPos)
      false
    }

  private def tryRead(b: Byte): Boolean =
    if(reader.nextOctet == b) {
      end.step(1)
      true
    } else {
      reader.seek(end.getStreamPos)
      false
    }

  def tryRead(ch: UChar): Boolean =
    if(tryRead(ch.toUtf8, 0)) {
      end.step(ch.getUtf8Length)
      true
    } else {
      false
    }

  def tryRead(str: UString): Boolean = if(tryRead(str.toUtf8, 0)) {
    end.step(cols = str.getLength, length = str.getUtf8Length)
    true
  } else {
    false
  }

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

  final def skipSpaces(): Unit = {
    while(tryReadWhiteSpace) {}
    commit()
  }

  def tryRead(test: Int => Boolean): Int = {
    val ch = reader.nextCodePoint
    if(test(ch)) {
      end.step(reader.getNumberOfLastOctetsRead)
      ch
    } else {
      reader.seek(end.getStreamPos)
      -1
    }
  }

  def tryReadDigit: Int = {
    val ch = reader.nextCodePoint
    if(UChar.isNumeric(ch)) {
      end.step(reader.getNumberOfLastOctetsRead)
      ch - '0'
    } else {
      reader.seek(end.getStreamPos)
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

  def readUChar: Option[UChar] = {
    val ch = reader.nextUChar
    if(ch.isDefined) {
      end.step(reader.getNumberOfLastOctetsRead)
    }
    ch
  }

  def getCurrentSelection: UString = UString.of(
    octets, begin.getStreamPos, end.getStreamPos - begin.getStreamPos)

  def getBegin: CodeLocation = begin

  def getCurrentLocation: CodeLocation = end.immutableCopy

  def hasMore: Boolean = end.getStreamPos < octets.length

  def commit(): CodeRange = {
    val b = begin
    begin = end.immutableCopy
    new CodeRange(file, b, begin)
  }

  def rollback(): Unit = {
    end.set(begin)
    reader.seek(end.getStreamPos)
  }

  def lexicalErrorAtBegining(message: String): LexicalError =
    new LexicalError(file, begin, message)

  def lexicalErrorAtCurrentLocation(message: String): LexicalError =
    new LexicalError(file, getCurrentLocation, message)
}
