package net.kfoundation.serialization

import net.kfoundation.UString
import net.kfoundation.UString._
import net.kfoundation.parse.lex.LexicalError
import org.scalatest.flatspec.AnyFlatSpec



class K4ObjectSerializerTest extends AnyFlatSpec {

  "Empty object" should "be deserialized" in {
    val input = U"TestObject[]"
    val expected = U"TestObject"
    val deserializer = new K4ObjectDeserializer(input)

    val beginToken = deserializer.readObjectBegin()
    assert(beginToken.value.contains(expected))

    val endToken = deserializer.readObjectEnd()
    assert(endToken.value.contains(expected))
  }

  "String property" should "be deserialized" in {
    val input: UString = "Object[field1=\"property value\"]"
    val expectedName = U"field1"
    val expectedValue = U"property value"
    val deserializer = new K4ObjectDeserializer(input)
    deserializer.readObjectBegin()

    val id = deserializer.readPropertyName()
    assert(id.equals(expectedName))

    val value = deserializer.readStringLiteral()
    assert(value.value.equals(expectedValue))
  }

  "Invalid string literal" should "result in error" in {
    val deserializer1 = new K4ObjectDeserializer("Object[field1=\"abc]")
    deserializer1.readObjectBegin()
    deserializer1.readPropertyName()
    assertThrows[LexicalError](deserializer1.readStringLiteral())

    val deserializer2 = new K4ObjectDeserializer("Object[field1=1234]")
    deserializer2.readObjectBegin()
    deserializer2.readPropertyName()
    assertThrows[LexicalError](deserializer2.readStringLiteral())
  }

  "Int property" should "be deserialized" in {
    val input = "Object[field1 = 1234567]"
    val expectedValue = 1234567
    val deserializer = new K4ObjectDeserializer(input)
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    assert(deserializer.readIntegerLiteral().value == expectedValue)
  }

  "Invalid integer value" should "result in error" in {
    val deserializer = new K4ObjectDeserializer("Object[field1=123.45]")
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    assertThrows[LexicalError](deserializer.readIntegerLiteral())
  }

  "Decimal property" should "be deserialized" in {
    val input = "Object[field1 = 123.4567]"
    val expectedValue: Double = 123.4567
    val deserializer = new K4ObjectDeserializer(input)
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    assert(deserializer.readDecimalLiteral().value == expectedValue)
  }

  "Invalid numeric value" should "result in error" in {
    val deserializer = new K4ObjectDeserializer("Object[field1=abc]")
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    assertThrows[LexicalError](deserializer.readDecimalLiteral())
  }

  "Boolean property" should "be deserialized" in {
    val input = "Object[field1=true field2=false]"
    val deserializer = new K4ObjectDeserializer(input)
    deserializer.readObjectBegin()

    deserializer.readPropertyName()
    assert(deserializer.readBooleanLiteral().value == true)

    deserializer.readPropertyName()
    assert(deserializer.readBooleanLiteral().value == false)
  }

  "Invalid boolean value" should "result in error" in {
    val deserializer = new K4ObjectDeserializer("Object[field1=abc]")
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    assertThrows[LexicalError](deserializer.readBooleanLiteral())
  }

  "Object with multiple literal properties" should "be deserialized" in {
    val input = "TestObject[property1=true property2=\"abc\" property3=123.345]"
    val typeName = U"TestObject"
    val deserializer = new K4ObjectDeserializer(input)
    assert(deserializer.readObjectBegin().value.contains(typeName))
    assert(deserializer.readPropertyName() == U"property1")
    assert(deserializer.readBooleanLiteral().value)
    assert(deserializer.readPropertyName() == U"property2")
    assert(deserializer.readStringLiteral().value == U"abc")
    assert(deserializer.readPropertyName() == U"property3")
    assert(deserializer.readDecimalLiteral().value == 123.345)
    assert(deserializer.readObjectEnd().value.contains(typeName))
  }

  "Nested objects" should "be deserialized" in {
    val input = "OuterObject[property=InnerObject[property=InnerInnerObject[]]]"
    val deserializer = new K4ObjectDeserializer(input)
    assert(deserializer.readObjectBegin().value.contains(U"OuterObject"))
    assert(deserializer.readPropertyName() == U"property")
    assert(deserializer.readObjectBegin().value.contains(U"InnerObject"))
    assert(deserializer.readPropertyName() == U"property")
    assert(deserializer.readObjectBegin().value.contains(U"InnerInnerObject"))
    assert(deserializer.readObjectEnd().value.contains(U"InnerInnerObject"))
    assert(deserializer.readObjectEnd().value.contains(U"InnerObject"))
    assert(deserializer.readObjectEnd().value.contains(U"OuterObject"))
  }

  "Empty collection" should "be deserialized" in {
    val deserializer = new K4ObjectDeserializer("Object[list={}]")
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    deserializer.readCollectionBegin()
    deserializer.tryReadCollectionEnd()
  }

  "Invalid collection being" should "result in error" in {
    val deserializer = new K4ObjectDeserializer("Object[list=123]")
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    assertThrows[LexicalError](deserializer.readCollectionBegin())
  }

  "Objects" should "be deserialized using reader" in {
    import ValueReadWriters._

    case class A(a1: UString, a2: Int, a3: Boolean)
    case class B(b1: Double)
    case class C(c1: A, c2: B)

    implicit val A_RW: ValueReadWriter[A] =
      readWriterOf[UString, Int, Boolean]("A", "a1", "a2", "a3").asReadWriterOf(
        v => A(v._1, v._2, v._3),
        a => (a.a1, a.a2, a.a3))

    implicit val B_RW: ValueReadWriter[B] =
      readWriterOf[Double]("B", "b1").asReadWriterOf(v => B(v), b => b.b1)

    implicit val C_RW: ValueReadWriter[C] =
      readWriterOf[A, B]("C", "c1", "c2").asReadWriterOf(
        v => C(v._1, v._2),
        c => (c.c1, c.c2))

    val input = "C[c1=A[a1=\"abc\" a2=123 a3=false] c2=B[b1=123.45]]"
    C_RW.read(new K4ObjectDeserializer(input))
  }
}