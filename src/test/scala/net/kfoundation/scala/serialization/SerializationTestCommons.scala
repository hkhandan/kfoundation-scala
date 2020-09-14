package net.kfoundation.scala.serialization

import net.kfoundation.scala.UString


object SerializationTestCommons {
  import ValueReadWriters._

  case class A(val a1: String, val a2: Int)
  case class B(val b1: Boolean, val b2: Double)
  case class C(val c1: Seq[A], val c2: B)

  implicit val A_RW = tuple("A", "a1" -> STRING, "a2" -> INT)
    .toReadWriterOf[A](
      a => A(a._1.toString(), a._2),
      a => (a.a1, a.a2))

  implicit val B_RW = tuple("B", "b1" -> BOOLEAN, "b2" -> DOUBLE)
    .toReadWriterOf[B](
      b => B(b._1, b._2),
      b => (b.b1, b.b2))

  implicit val C_RW = tuple("C", "c1" -> A_RW.seq, "c2" -> B_RW)
    .toReadWriterOf[C](
      c => C(c._1, c._2),
      c => (c.c1, c.c2))
}
