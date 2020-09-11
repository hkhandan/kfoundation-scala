package net.kfoundation.scala.serialization

import java.io.ByteArrayOutputStream

import net.kfoundation.scala.UString

object SerializationTestCommons {
  import ValueReadWriters._

  case class A(val a1: String, val a2: Int)
  case class B(val b1: Boolean, val b2: Double)
  case class C(val c1: Array[A], val c2: B)

  implicit val A_WRITER = ValueWriters.writerOf[UString, Int]("A", "a1", "a2")
    .toWriterOf[A](a => (a.a1, a.a2))

  implicit val A_ARRAY_WRITER = A_WRITER.arrayWriter

  implicit val B_WRITER = ValueWriters.writerOf[Boolean, Double]("B", "b1", "b2")
    .toWriterOf[B](b => (b.b1, b.b2))

  implicit val C_WRITER = ValueWriters.writerOf[Array[A], B]("C", "c1", "c2")
    .toWriterOf[C](c => (c.c1, c.c2))
}
