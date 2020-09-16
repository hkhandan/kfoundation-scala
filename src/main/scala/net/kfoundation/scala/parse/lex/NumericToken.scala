// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse.lex

import net.kfoundation.scala.UChar
import net.kfoundation.scala.parse.CodeRange

import scala.annotation.tailrec



object NumericToken {
  private val PLUS : UChar = '+'
  private val MINUS: UChar = '-'
  private val DOT: UChar = '.'
  private val SMALL_E: UChar = 'e'
  private val BIG_E: UChar = 'E'
  private val ZERO: UChar = '0'


  private def readSign(w: CodeWalker): Int =
    if(w.tryRead(PLUS)) {
      1
    } else if(w.tryRead(MINUS)) {
      -1
    } else {
      1
    }


  private def readIntPart(w: CodeWalker): Option[Long] = {
    val hasZeros = w.readAll(ZERO) > 0

    val d: Int = w.tryReadDigit
    if(d < 0) {
      if(hasZeros) {
        Some(0)
      } else {
        None
      }
    } else {
      Some(readIntPart(w, d))
    }
  }


  @tailrec
  private def readIntPart(w: CodeWalker, n: Long): Long = {
    val d = w.tryReadDigit
    if(d < 0) {
      n
    } else {
      readIntPart(w, n*10 + d)
    }
  }


  @tailrec
  private def readFractionPart(w: CodeWalker, n: Double, m: Double): Option[Double] = {
    val d = w.tryReadDigit
    if(d < 0) {
      if(m == 10) {
        None
      } else {
        Some(n)
      }
    } else {
      readFractionPart(w, n + d/m, m*10)
    }
  }


  private def readExponentPart(w: CodeWalker): Option[Long] = {
    val s = readSign(w)
    val n = readIntPart(w)
    n.map(_ * s)
      .orElse(throw w.lexicalErrorAtCurrentLocation("Exponent value is missing"))
  }


  private def tryReadPart2(w: CodeWalker): Option[Double] =
    if(w.tryRead(DOT)) {
      readFractionPart(w, 0, 10)
    } else {
      None
    }


  private def tryReadPart3(w: CodeWalker): Option[Long] =
    if(w.tryRead(SMALL_E)) {
      readExponentPart(w)
    } else if(w.tryRead(BIG_E)) {
      readExponentPart(w)
    } else {
      None
    }


  private def composeDecimalToken(bounds: CodeRange, s: Int, part1: Long,
    part2: Double, part3: Option[Long]): DecimalToken =
  {
    val p3 = part3.map(i => Math.pow(10, i.toDouble))
      .getOrElse[Double](1)
    new DecimalToken(bounds, (part1 + part2) * p3 * s)
  }


  /**
   * Attempts to read an integral or fractional decimal number or one with
   * scientific notional from input stream. The result will be a subclass of
   * DecimalToken or IntegralToken depending on whether the input has
   * fractional part or not.
   *
   * A number is of the form:
   * <pre>
   * [+-](part1)(.part2)[eE][+-](part3)
   * </pre>
   */
  object reader extends TokenReader[NumericToken[_]] {
    override def tryRead(w: CodeWalker): Option[NumericToken[_]] = {
      val s = readSign(w)

      readIntPart(w).map(part1 => {
        val part2 = tryReadPart2(w)
        val part3 = tryReadPart3(w)
        val bounds = w.commit()
        if (part2.isEmpty && part3.isEmpty) {
          new IntegralToken(bounds, part1 * s)
        } else {
          composeDecimalToken(bounds, s, part1, part2.getOrElse(0), part3)
        }
      }).orElse({
        tryReadPart2(w).map(part2 => {
          val part3 = tryReadPart3(w)
          val bounds = w.commit()
          composeDecimalToken(bounds, s, 0, part2, part3)
        })
      })
    }
  }

}



/** A portion of input text that represents a number */
abstract class NumericToken[T](range: CodeRange, value: T)
  extends Token[T](range, value)