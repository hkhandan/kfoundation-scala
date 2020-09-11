package net.kfoundation.scala.serialization
import java.io.InputStream

import net.kfoundation.scala.UString
import net.kfoundation.scala.io.Path
import net.kfoundation.scala.parse.CodeLocation
import net.kfoundation.scala.parse.lex._



object JsonObjectDeserializer {

  val FACTORY: ObjectDeserializerFactory = new ObjectDeserializerFactory {
    override def of(str: UString): ObjectDeserializer =
      new JsonObjectDeserializer(CodeWalker.of(str))

    override def of(input: InputStream): ObjectDeserializer =
      new JsonObjectDeserializer(CodeWalker.of(input))

    override def of(path: Path): ObjectDeserializer
    = new JsonObjectDeserializer(CodeWalker.of(path))
  }

}



class JsonObjectDeserializer private (walker: CodeWalker) extends ObjectDeserializer {
  import internals.CommonSymbols._


  override def readObjectBegin(): Option[UString] = {
    walker.skipSpaces()
    if(walker.tryRead(OPEN_CURLY_BRACE)) {
      walker.commit()
      None
    } else {
      throw walker.lexicalErrorAtBeginning("'{' expected")
    }
  }


  override def readObjectEnd(): Option[UString] = {
    walker.skipSpaces()
    if(walker.tryRead(CLOSE_CURLY_BRACE)) {
      walker.commit()
      None
    } else {
      throw walker.lexicalErrorAtBeginning("'}' expected")
    }
  }


  override def readCollectionBegin(): Unit = {
    walker.skipSpaces()
    if(walker.tryRead(OPEN_BRACE)) {
      walker.commit()
    } else {
      throw walker.lexicalErrorAtBeginning("']' expected")
    }
  }


  override def tryReadCollectionEnd(): Boolean = {
    walker.skipSpaces()
    if(walker.tryRead(CLOSE_BRACE)) {
      walker.commit()
      true
    } else {
      false
    }
  }


  override def tryReadPropertyName(): Option[UString] = {
    walker.skipSpaces()
    IdentifierToken.reader.tryRead(walker).map(token => {
      if(!walker.tryRead(COLON)) {
        throw walker.lexicalErrorAtBeginning("':' expected")
      }
      walker.commit()
      token.value
    })
  }


  override def readStringLiteral(): UString = StringToken.reader
    .tryRead(walker)
    .getOrElse(throw walker.lexicalErrorAtBeginning("String literal expected"))
    .value


  override def readIntegerLiteral(): Long = NumericToken.reader
    .tryRead(walker) match {
      case Some(i: IntegralToken) => i.value
      case Some(d: DecimalToken) => throw walker.lexicalErrorAtCurrentLocation("Expected an integer but found: " + d)
      case _ => throw walker.lexicalErrorAtCurrentLocation("Integral value expected")
    }


  override def readDecimalLiteral(): Double = NumericToken.reader
    .tryRead(walker) match {
      case Some(i: IntegralToken) => i.value
      case Some(d: DecimalToken) => d.value
      case _ => throw walker.lexicalErrorAtCurrentLocation("Decimal value expected")
    }


  override def readBooleanLiteral(): Boolean =
    if(walker.tryRead(TRUE)) {
      walker.commit()
      true
    } else if(walker.tryRead(FALSE)) {
      walker.commit()
      false
    } else {
      throw walker.lexicalErrorAtBeginning("Expected a boolean value")
    }


  override protected def getCurrentLocation: CodeLocation =
    walker.getCurrentLocation
}
