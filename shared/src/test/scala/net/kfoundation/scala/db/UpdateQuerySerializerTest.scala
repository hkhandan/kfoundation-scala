package net.kfoundation.scala.db

import net.kfoundation.scala.UString
import net.kfoundation.scala.db.Commons.{NestedObject, SimpleObject}
import org.scalatest.funsuite.AnyFunSuite



class UpdateQuerySerializerTest extends AnyFunSuite {

  test("Update simple object") {
    val input = SimpleObject(1, 2, true, "one two tree")
    val expected: UString =
      """update "SimpleObject" set
        |  a=1,
        |  b=2,
        |  c=true,
        |  d='one two tree'""".stripMargin

    val actual = Commons.SIMPLE_RW
      .toString(UpdateQueryObjectSerializer.FACTORY, input)

    assert(actual == expected)
  }


  test("Update nested object") {
    val input = NestedObject(
      SimpleObject(1, 2, true, "one two tree"),
      1.234)

    val expected: UString =
      """update "NestedObject" set
        |  e_a=1,
        |  e_b=2,
        |  e_c=true,
        |  e_d='one two tree',
        |  f=1.234""".stripMargin

    val actual = Commons.NESTED_RW
      .toString(UpdateQueryObjectSerializer.FACTORY, input)

    assert(actual == expected)
  }

}
