package net.kfoundation.scala.serialization

import net.kfoundation.scala.UString


object SerializationTestCommons {
  import ValueReadWriters._

  case class A(val a1: String, val a2: Int)
  case class B(val b1: Boolean, val b2: Double)
  case class C(val c1: Seq[A], val c2: B)

  implicit val A_RW = ValueReadWriters.readWriterOf[UString, Int]("A", "a1", "a2")
    .toReadWriterOf[A](
      a => A(a._1.toString(), a._2),
      a => (a.a1, a.a2))

  implicit val A_ARRAY_RW = A_RW.seqReadWriter

  implicit val B_RW = ValueReadWriters.readWriterOf[Boolean, Double]("B", "b1", "b2")
    .toReadWriterOf[B](
      b => B(b._1, b._2),
      b => (b.b1, b.b2))

  implicit val C_RW = ValueReadWriters.readWriterOf[Seq[A], B]("C", "c1", "c2")
    .toReadWriterOf[C](
      c => C(c._1, c._2),
      c => (c.c1, c.c2))
}
