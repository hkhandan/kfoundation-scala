// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization
import java.io.InputStream

import net.kfoundation.scala.UString
import net.kfoundation.scala.io.Path
import net.kfoundation.scala.parse.CodeLocation
import net.kfoundation.scala.parse.lex._
import net.kfoundation.scala.serialization.internals.ObjectStreamStateMachine



object JsonObjectDeserializer {
  val MIME_TYPE: UString = "application/json"

  val FACTORY: ObjectDeserializerFactory = new ObjectDeserializerFactory {
    override def of(input: InputStream): ObjectDeserializer =
      new JsonObjectDeserializer(CodeWalker.of(input))

    override def getMediaType: UString = MIME_TYPE
  }
}


/** JSON object deserializer. */
class JsonObjectDeserializer private(walker: CodeWalker) extends ObjectDeserializer {
  import internals.CommonSymbols._

  private val stateMachine = new ObjectStreamStateMachine

  override def readObjectBegin(): Option[UString] = {
    if(stateMachine.isInCollection && !stateMachine.isFirst) {
      walker.skipSpaces()
      if(!walker.tryRead(COMMA)) {
        throw walker.lexicalErrorAtCurrentLocation("',' expected")
      }
    }
    stateMachine.objectBegin()
    walker.skipSpaces()
    if(walker.tryRead(OPEN_CURLY_BRACE)) {
      walker.commit()
      None
    } else {
      throw walker.lexicalErrorAtBeginning("'{' expected")
    }
  }


  override def readObjectEnd(): Option[UString] = {
    stateMachine.objectEnd()
    walker.skipSpaces()
    if(walker.tryRead(CLOSE_CURLY_BRACE)) {
      walker.commit()
      None
    } else {
      throw walker.lexicalErrorAtBeginning("'}' expected")
    }
  }


  override def readCollectionBegin(): Unit = {
    stateMachine.collectionBegin()
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
      stateMachine.collectionEnd()
      true
    } else {
      false
    }
  }


  override def tryReadPropertyName(): Option[UString] =
    if(!stateMachine.isFirst && !walker.tryRead(COMMA)) {
      walker.skipSpaces()
      None
    } else {
      walker.skipSpaces()
      StringToken.reader.tryRead(walker).map(token => {
        if(!walker.tryRead(COLON)) {
          throw walker.lexicalErrorAtBeginning("':' expected")
        }
        walker.commit()
        stateMachine.property()
        token.value
      })
    }


  override def readStringLiteral(): UString = {
    stateMachine.literal()
    walker.skipSpaces()
    StringToken.reader
      .tryRead(walker)
      .getOrElse(throw walker.lexicalErrorAtBeginning("String literal expected"))
      .value
  }


  override def readIntegerLiteral(): Long = {
    stateMachine.literal()
    walker.skipSpaces()
    NumericToken.reader
      .tryRead(walker) match {
      case Some(i: IntegralToken) => i.value
      case Some(d: DecimalToken) => throw walker.lexicalErrorAtCurrentLocation("Expected an integer but found: " + d)
      case _ => throw walker.lexicalErrorAtCurrentLocation("Integral value expected")
    }
  }


  override def readDecimalLiteral(): Double = {
    stateMachine.literal()
    walker.skipSpaces()
    NumericToken.reader
      .tryRead(walker) match {
      case Some(i: IntegralToken) => (i.value).toDouble
      case Some(d: DecimalToken) => d.value
      case _ => throw walker.lexicalErrorAtCurrentLocation("Decimal value expected")
    }
  }


  override def readBooleanLiteral(): Boolean = {
    stateMachine.literal()
    walker.skipSpaces()
    if(walker.tryRead(TRUE)) {
      walker.commit()
      true
    } else if(walker.tryRead(FALSE)) {
      walker.commit()
      false
    } else {
      throw walker.lexicalErrorAtBeginning("Expected a boolean value")
    }
  }


  override protected def getCurrentLocation: CodeLocation =
    walker.getCurrentLocation
}
