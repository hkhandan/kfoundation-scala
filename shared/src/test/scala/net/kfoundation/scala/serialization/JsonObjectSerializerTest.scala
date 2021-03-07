// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization

import net.kfoundation.scala.UString.Interpolator
import org.scalatest.flatspec.AnyFlatSpec


class JsonObjectSerializerTest extends AnyFlatSpec {
  import SerializationTestCommons._

  "Object" should "be serialized" in {
    val input = C(
      Array(
        A("one", 1),
        A("two", 2),
        A("three", 3)),
      B(false, 123.456))

    val expected = U"""{
       |  c1: [
       |    {
       |      a1: "one",
       |      a2: 1
       |    }, {
       |      a1: "two",
       |      a2: 2
       |    }, {
       |      a1: "three",
       |      a2: 3
       |    }
       |  ],
       |  c2: {
       |    b1: false,
       |    b2: 123.456
       |  }
       |}""".stripMargin

    val output = C_RW.toString(JsonObjectSerializer.FACTORY, input)

    assert(expected == output)
  }

}
