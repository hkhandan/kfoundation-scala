// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization

import net.kfoundation.scala.UString


object SerializationTestCommons {
  import ValueReadWriters._

  case class A(val a1: String, val a2: Int)
  case class B(val b1: Boolean, val b2: Double)
  case class C(val c1: Seq[A], val c2: B)

  implicit val A_RW = tuple[UString, Int]("A", "a1" -> STRING, "a2" -> INT)
    .mapRW[A](
      a => A(a._1.toString(), a._2),
      a => (a.a1, a.a2))

  implicit val B_RW = tuple("B", "b1" -> BOOLEAN, "b2" -> DOUBLE)
    .mapRW[B](
      b => B(b._1, b._2),
      b => (b.b1, b.b2))

  implicit val C_RW = tuple("C", "c1" -> A_RW.seq, "c2" -> B_RW)
    .mapRW[C](
      c => C(c._1, c._2),
      c => (c.c1, c.c2))
}
