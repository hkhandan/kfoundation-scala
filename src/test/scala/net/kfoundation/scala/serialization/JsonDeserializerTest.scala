package net.kfoundation.scala.serialization

import net.kfoundation.scala.UString.UStringInterpolation
import org.scalatest.flatspec.AnyFlatSpec

class JsonDeserializerTest extends AnyFlatSpec {
  import SerializationTestCommons._

  "String" should "be deserialized" in {
    val input =
      U"""{
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

    val expected = C(List(A("one",1), A("two",2), A("three",3)),B(false,123.456))

    val output = C_RW.read(JsonObjectDeserializer.FACTORY, input)

    assert(output == expected)
  }

}
