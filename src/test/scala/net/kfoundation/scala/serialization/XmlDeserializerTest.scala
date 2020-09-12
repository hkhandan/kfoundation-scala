package net.kfoundation.scala.serialization

import net.kfoundation.scala.UString.UStringInterpolation
import org.scalatest.flatspec.AnyFlatSpec

class XmlDeserializerTest extends AnyFlatSpec {
  import SerializationTestCommons._

  "String" should "be deserialized" in {
    val input =
      U"""<C>
         |    <c1>
         |        <A>
         |            <a1>one</a1>
         |            <a2>1</a2>
         |        </A><A>
         |            <a1>two</a1>
         |            <a2>2</a2>
         |        </A><A>
         |            <a1>three</a1>
         |            <a2>3</a2>
         |        </A>
         |    </c1>
         |    <c2>
         |        <b1>false</b1>
         |        <b2>123.456</b2>
         |    </c2>
         |</C>""".stripMargin

    val expected = C(List(A("one",1), A("two",2), A("three",3)),B(false,123.456))

    val output = C_RW.read(XmlObjectDeserializer.FACTORY, input)

    assert(output == expected)
  }

}
