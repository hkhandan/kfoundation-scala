package net.kfoundation.scala.serialization

import org.scalatest.flatspec.AnyFlatSpec

class YamSerializerTest extends AnyFlatSpec {
  import SerializationTestCommons._

  "Object" should "be serialized" in {
    val input = C(
      Array(
        A("one", 1),
        A("two", 2),
        A("three", 3)),
      B(false, 123.456))

    val output = C_WRITER.toString(YamlObjectSerializer.FACTORY, input)

    println(output)
  }


}
