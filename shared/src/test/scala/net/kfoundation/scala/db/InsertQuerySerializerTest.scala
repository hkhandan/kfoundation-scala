package net.kfoundation.scala.db

import net.kfoundation.scala.UString
import net.kfoundation.scala.db.Commons.{NestedObject, SimpleObject}
import org.scalatest.funsuite.AnyFunSuite

class InsertQuerySerializerTest extends AnyFunSuite {

  test("Update simple object") {
    val input = SimpleObject(1, 2, true, "one two tree")
    val expected: UString =
      """insert into "SimpleObject"(a, b, c, d) values(
        |  1, 2, true, 'one two tree')""".stripMargin

    val actual = Commons.SIMPLE_RW
      .toString(InsertQueryObjectSerializer.FACTORY, input)

    assert(actual == expected)
  }


  test("Update nested object") {
    val input = NestedObject(
      SimpleObject(1, 2, true, "one two tree"),
      1.234)

    val expected: UString =
      """insert into "NestedObject"(e_a, e_b, e_c, e_d, f) values(
        |  1, 2, true, 'one two tree', 1.234)""".stripMargin

    val actual = Commons.NESTED_RW
      .toString(InsertQueryObjectSerializer.FACTORY, input)

    assert(actual == expected)
  }
}
