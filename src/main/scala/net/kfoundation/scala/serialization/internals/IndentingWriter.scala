package net.kfoundation.scala.serialization.internals

import java.io.OutputStream

import net.kfoundation.scala.util.Loop
import net.kfoundation.scala.{UChar, UString}



object IndentingWriter {
  private val SPACE = ' '
  private val NEWLINE = '\n'
}



class IndentingWriter(output: OutputStream, indentSize: Int, compact: Boolean) {
  import IndentingWriter._

  private val isNewLineEnabled = !compact
  private var idt: Int = 0
  private var isAtNewLine = isNewLineEnabled


  private def writeIndent(): Unit =
    Loop(idt * indentSize, _ => output.write(SPACE))


  def indent(): Unit = idt += 1


  def unindent(): Unit = {
    idt -= 1
    if (idt < 0) {
      throw new IllegalStateException("Too many un-indents")
    }
  }


  def writeNewLine(): Unit =
    if(isNewLineEnabled) {
      output.write(NEWLINE)
      isAtNewLine = true
    }


  def write(ch: Char): Unit = {
    if(isAtNewLine) {
      writeIndent()
      isAtNewLine = false
    }
    output.write(ch)
  }

  def write(ch1: Char, ch2: Char): Unit = {
    if(isAtNewLine) {
      writeIndent()
      isAtNewLine = false
    }
    output.write(ch1)
    output.write(ch2)
  }

  def write(ch: UChar): Unit = {
    if (isAtNewLine) {
      writeIndent()
      isAtNewLine = false
    }
    ch.writeToStream(output)
  }

  def write(str: UString): Unit = {
    if (isAtNewLine) {
      writeIndent()
      isAtNewLine = false
    }
    str.writeToStream(output)
  }

  def writeln(str: UString): Unit = {
    write(str)
    writeNewLine()
  }
}
