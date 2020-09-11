package net.kfoundation.scala.serialization

import net.kfoundation.scala.UString
import net.kfoundation.scala.UString._
import net.kfoundation.scala.parse.lex.LexicalError
import org.scalatest.flatspec.AnyFlatSpec



class K4ObjectDeserializerTest extends AnyFlatSpec {
  import K4ObjectDeserializer.FACTORY

  "Empty object" should "be deserialized" in {
    val input = U"TestObject[]"
    val expected = U"TestObject"
    val deserializer = FACTORY.of(input)

    val beginToken = deserializer.readObjectBegin()
    assert(beginToken.contains(expected))

    val endToken = deserializer.readObjectEnd()
    assert(endToken.contains(expected))
  }

  "String property" should "be deserialized" in {
    val input: UString = "Object[field1=\"property value\"]"
    val expectedName = U"field1"
    val expectedValue = U"property value"
    val deserializer = FACTORY.of(input)
    deserializer.readObjectBegin()

    val id = deserializer.readPropertyName()
    assert(id.equals(expectedName))

    val value = deserializer.readStringLiteral()
    assert(value.equals(expectedValue))
  }

  "Invalid string literal" should "result in error" in {
    val deserializer1 = FACTORY.of("Object[field1=\"abc]")
    deserializer1.readObjectBegin()
    deserializer1.readPropertyName()
    assertThrows[LexicalError](deserializer1.readStringLiteral())

    val deserializer2 = FACTORY.of("Object[field1=1234]")
    deserializer2.readObjectBegin()
    deserializer2.readPropertyName()
    assertThrows[LexicalError](deserializer2.readStringLiteral())
  }

  "Int property" should "be deserialized" in {
    val input = "Object[field1 = 1234567]"
    val expectedValue = 1234567
    val deserializer = FACTORY.of(input)
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    assert(deserializer.readIntegerLiteral() == expectedValue)
  }

  "Invalid integer value" should "result in error" in {
    val deserializer = FACTORY.of("Object[field1=123.45]")
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    assertThrows[LexicalError](deserializer.readIntegerLiteral())
  }

  "Decimal property" should "be deserialized" in {
    val input = "Object[field1 = 123.4567]"
    val expectedValue: Double = 123.4567
    val deserializer = FACTORY.of(input)
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    assert(deserializer.readDecimalLiteral() == expectedValue)
  }

  "Invalid numeric value" should "result in error" in {
    val deserializer = FACTORY.of("Object[field1=abc]")
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    assertThrows[LexicalError](deserializer.readDecimalLiteral())
  }

  "Boolean property" should "be deserialized" in {
    val input = "Object[field1=true field2=false]"
    val deserializer = FACTORY.of(input)
    deserializer.readObjectBegin()

    deserializer.readPropertyName()
    assert(deserializer.readBooleanLiteral())

    deserializer.readPropertyName()
    assert(!deserializer.readBooleanLiteral())
  }

  "Invalid boolean value" should "result in error" in {
    val deserializer = FACTORY.of("Object[field1=abc]")
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    assertThrows[LexicalError](deserializer.readBooleanLiteral())
  }

  "Object with multiple literal properties" should "be deserialized" in {
    val input = "TestObject[property1=true property2=\"abc\" property3=123.345]"
    val typeName = U"TestObject"
    val deserializer = FACTORY.of(input)
    assert(deserializer.readObjectBegin().contains(typeName))
    assert(deserializer.readPropertyName() == U"property1")
    assert(deserializer.readBooleanLiteral())
    assert(deserializer.readPropertyName() == U"property2")
    assert(deserializer.readStringLiteral() == U"abc")
    assert(deserializer.readPropertyName() == U"property3")
    assert(deserializer.readDecimalLiteral() == 123.345)
    assert(deserializer.readObjectEnd().contains(typeName))
  }

  "Nested objects" should "be deserialized" in {
    val input = "OuterObject[property=InnerObject[property=InnerInnerObject[]]]"
    val deserializer = FACTORY.of(input)
    assert(deserializer.readObjectBegin().contains(U"OuterObject"))
    assert(deserializer.readPropertyName() == U"property")
    assert(deserializer.readObjectBegin().contains(U"InnerObject"))
    assert(deserializer.readPropertyName() == U"property")
    assert(deserializer.readObjectBegin().contains(U"InnerInnerObject"))
    assert(deserializer.readObjectEnd().contains(U"InnerInnerObject"))
    assert(deserializer.readObjectEnd().contains(U"InnerObject"))
    assert(deserializer.readObjectEnd().contains(U"OuterObject"))
  }

  "Empty collection" should "be deserialized" in {
    val deserializer = FACTORY.of("Object[list={}]")
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    deserializer.readCollectionBegin()
    deserializer.tryReadCollectionEnd()
  }

  "Invalid collection being" should "result in error" in {
    val deserializer = FACTORY.of("Object[list=123]")
    deserializer.readObjectBegin()
    deserializer.readPropertyName()
    assertThrows[LexicalError](deserializer.readCollectionBegin())
  }

  "Objects" should "be deserialized using reader" in {
    import ValueReadWriters._

    case class A(a1: UString, a2: Int, a3: Boolean)
    case class B(b1: Double)
    case class C(c1: A, c2: B)

    implicit val aRW: ValueReadWriter[A] =
      readWriterOf[UString, Int, Boolean]("A", "a1", "a2", "a3").toReadWriterOf(
        v => A(v._1, v._2, v._3),
        a => (a.a1, a.a2, a.a3))

    implicit val bRW: ValueReadWriter[B] =
      readWriterOf[Double]("B", "b1").toReadWriterOf(v => B(v), b => b.b1)

    implicit val cRW: ValueReadWriter[C] =
      readWriterOf[A, B]("C", "c1", "c2").toReadWriterOf(
        v => C(v._1, v._2),
        c => (c.c1, c.c2))

    val input = "C[c1=A[a1=\"abc\" a2=123 a3=false] c2=B[b1=123.45]]"
    val expected = C(A("abc", 123, a3 = false), B(123.45))

    val c = cRW.read(FACTORY.of(input))

    assert(c == expected)
  }

}