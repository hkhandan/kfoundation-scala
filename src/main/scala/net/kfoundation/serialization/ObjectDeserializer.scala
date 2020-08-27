package net.kfoundation.serialization

import net.kfoundation.UString
import net.kfoundation.parse.lex._
import net.kfoundation.parse.{CodeLocation, CodeRange}

import scala.annotation.tailrec
import scala.collection.mutable


object ObjectDeserializer {
  class ObjectBeginToken(range: CodeRange, cls: Option[UString])
    extends Token[Option[UString]](range, cls)

  class ObjectEndToken(range: CodeRange, cls: Option[UString])
    extends Token[Option[UString]](range, cls)

  class CollectionBeginToken(range: CodeRange) extends Token[Unit](range, ())
  class CollectionEndToken(range: CodeRange) extends Token[Unit](range, ())
  class PropertyNameToken(range: CodeRange, name: UString)
    extends IdentifierToken(range, name)
}


trait ObjectDeserializer {
  import ObjectDeserializer._

  // Literals are: String, Int, Boolean, Real
  // Literals are attributes
  // Enum is object with mandatory "name" property. Optionally, other properties can be serialized.
  // Object with name can be deserialized as enum, other properties will be ignored.
  // Array, Seq, List, etc. are collection
  // XML tag name for collection is "Collection" by default, can be overridden
  // Map is object with mandatory property "elements" collection of KV objects.
  //   Literal keys are attribute in XML and literal in JSON
  // Null in XML has mandatory empty attribute "xsi:nil"

  def readObjectBegin(): ObjectBeginToken
  def readObjectEnd(): ObjectEndToken
  def readCollectionBegin(): CollectionBeginToken
  def tryReadCollectionEnd(): Option[CollectionEndToken]
  def readStringLiteral(): StringToken
  def readIntegerLiteral(): IntegralToken
  def readDecimalLiteral(): DecimalToken
  def readBooleanLiteral(): BooleanToken
  def tryReadPropertyName(): Option[PropertyNameToken]
  def getCurrentLocation: CodeLocation


  private def getLocationTag: String = getCurrentLocation.getLocationTag


  def readObjectBegin(expectedName: UString): Unit = readObjectBegin().value
    .foreach(name => if(!name.equals(expectedName))
      throw new DeserializationError(
        s"At $getCurrentLocation object of type $expectedName was expected but found: $name"))


  def readPropertyName(): UString = tryReadPropertyName()
    .getOrElse(throw new LexicalError(
      getCurrentLocation,
      "Missing expected property name"))
    .value


  def readObject(typeName: UString, fn: UString => Unit): Unit = {
    readObjectBegin(typeName)

    @tailrec
    def readNextProps(): Unit = {
      val maybeProp = tryReadPropertyName()
      if(maybeProp.isDefined) {
        fn(maybeProp.get.value)
        readNextProps()
      }
    }

    readNextProps()
    readObjectEnd()
  }


//  def readObject(typeName: UString, fields: Map[UString, ValueReader[_]]):
//  Map[UString, Any] = {
//    readObjectBegin(typeName)
//
//    val values = new mutable.HashMap[UString, Any]()
//
//    @tailrec
//    def readNextProps(): Unit = {
//      val maybeProp = tryReadPropertyName()
//      if(maybeProp.isDefined) {
//        val fieldName = maybeProp.get.value
//
//        fields.get(fieldName)
//          .map(reader => {values.put(fieldName, reader.read(this)); ()})
//          .orElse(throw new DeserializationError(
//            s"$getLocationTag no reader provided for field $fieldName of type $typeName"))
//
//        readNextProps()
//      }
//    }
//
//    readNextProps()
//    values.toMap
//  }
}
