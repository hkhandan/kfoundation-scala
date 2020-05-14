package net.kfoundation.lang.lex

import net.kfoundation.UChar
import net.kfoundation.lang.CodeRange

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
    val hasZeros = w.tryReadAll(ZERO) > 0

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

  object reader extends TokenReader[NumericToken[_]] {
    override def tryRead(w: CodeWalker): Option[NumericToken[_]] = {
      // (+-)[part1](.[part2])e(+-)[part3]

      val s = readSign(w)

      val part1: Option[Long] = readIntPart(w)

      val part2 = if(w.tryRead(DOT)) {
        readFractionPart(w, 0, 10)
      } else {
        None
      }

      val part3 = if(w.tryRead(SMALL_E)) {
        readExponentPart(w)
      } else if(w.tryRead(BIG_E)) {
        readExponentPart(w)
      } else {
        None
      }

      if(part2.isEmpty && part3.isEmpty) {
        if(part1.isEmpty) {
          w.rollback()
          None
        } else {
          val bounds = w.commit()
          Some(new IntegralToken(bounds, part1.get * s))
        }
      } else if(part2.isDefined) {
        val bounds = w.commit()
        val p1 = part1.getOrElse[Long](0)
        val p2 = part2.getOrElse[Double](0)
        val p3 = part3.map(i => Math.pow(10, i))
          .getOrElse[Double](1)
        Some(new DecimalToken(bounds, (p1 + p2) * p3 * s))
      } else {
        None
      }
    }
  }
}


abstract class NumericToken[T](range: CodeRange, value: T)
  extends Token[T](range, value)