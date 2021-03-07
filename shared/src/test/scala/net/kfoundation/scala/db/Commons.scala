package net.kfoundation.scala.db

import net.kfoundation.scala.UString
import net.kfoundation.scala.serialization.ValueReadWriter



object Commons {
  import net.kfoundation.scala.serialization.ValueReadWriters._


  case class SimpleObject(a: Int, b: Long, c: Boolean, d: UString)
  case class NestedObject(e: SimpleObject, f: Double)


  implicit val SIMPLE_RW: ValueReadWriter[SimpleObject] =
    tuple("SimpleObject", "a" -> INT, "b" -> LONG,
      "c" -> BOOLEAN, "d" -> STRING)
      .mapRW(
        t => SimpleObject(t._1, t._2, t._3, t._4),
        s => (s.a, s.b, s.c, s.d))


  implicit val NESTED_RW: ValueReadWriter[NestedObject] =
    tuple("NestedObject", "e" -> SIMPLE_RW, "f" -> DOUBLE)
      .mapRW(
        t => NestedObject(t._1, t._2),
        s => (s.e, s.f))
}
