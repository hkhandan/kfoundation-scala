// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization

import net.kfoundation.scala.io.Path
import net.kfoundation.scala.parse.CodeLocation
import net.kfoundation.scala.parse.lex._
import net.kfoundation.scala.serialization.internals.ObjectStreamStateMachine
import net.kfoundation.scala.{UChar, UString}

import java.io.InputStream



/**  */
object K4ObjectDeserializer {
  val MIME_TYPE: UString = "application/x-k4"

  val FACTORY: ObjectDeserializerFactory = new ObjectDeserializerFactory {
    override def of(input: InputStream): ObjectDeserializer =
      new K4ObjectDeserializer(CodeWalker.of(input))

    override def getMediaType: UString = MIME_TYPE
  }
}



/** K4 object deserializer */
class K4ObjectDeserializer private (walker: CodeWalker)
  extends ObjectDeserializer
{
  import internals.CommonSymbols._

  private val stateMachine = new ObjectStreamStateMachine

  private def readOrError(symbol: UChar): Unit = {
    if(!walker.tryRead(symbol)) {
      throw walker.lexicalErrorAtCurrentLocation(
        "Missing expected symbol '" + symbol.toString + "'")
    }
  }

  override def readObjectBegin(): Option[UString] = {
    if(stateMachine.isInCollection && !stateMachine.isFirst) {
      walker.skipSpaces()
      if(!walker.tryRead(COMMA)) {
        throw walker.lexicalErrorAtCurrentLocation("',' expected")
      }
    }

    walker.skipSpaces()

    val token = IdentifierToken.reader
      .tryRead(walker)
      .getOrElse(throw walker.lexicalErrorAtBeginning(
        "Missing expected identifier"))

    walker.readSpaces()
    readOrError(OPEN_BRACE)
    stateMachine.objectBegin(token.value)
    Some(token.value)
  }


  override def readObjectEnd(): Option[UString] = {
    walker.skipSpaces()
    readOrError(CLOSE_BRACE)
    val name = stateMachine.objectEnd()
    walker.commit()
    name
  }


  override def readCollectionBegin(): Unit = {
    walker.skipSpaces()
    readOrError(OPEN_CURLY_BRACE)
    stateMachine.collectionBegin()
    walker.commit()
  }


  override def tryReadCollectionEnd(): Boolean = {
    walker.skipSpaces()
    if(!walker.tryRead(CLOSE_CURLY_BRACE)) {
      false
    } else {
      stateMachine.collectionEnd()
      true
    }
  }


  override def tryReadPropertyName(): Option[UString] = {
    walker.skipSpaces()

    IdentifierToken.reader.tryRead(walker).map(id => {
      walker.readSpaces()
      readOrError(EQUAL)
      stateMachine.property()
      id.value
    })
  }


  override def readStringLiteral(): UString = {
    stateMachine.literal()
    walker.skipSpaces()
    StringToken.reader
      .tryRead(walker)
      .getOrElse(throw walker.lexicalErrorAtBeginning("Missing string literal"))
      .value
  }


  override def readIntegerLiteral(): Long = {
    stateMachine.literal()
    walker.skipSpaces()
    NumericToken.reader.tryRead(walker) match {
      case Some(i: IntegralToken) => i.value
      case Some(d: DecimalToken) => throw walker.lexicalErrorAtBeginning(
        "Expected an integer, found: " + d.value)
      case _ => throw walker.lexicalErrorAtBeginning("Missing expected integer")
    }
  }


  override def readDecimalLiteral(): Double = {
    stateMachine.literal()
    walker.skipSpaces()
    NumericToken.reader.tryRead(walker) match {
      case Some(i: IntegralToken) => i.asDecimalToken.value
      case Some(d: DecimalToken) => d.value
      case _ => throw walker.lexicalErrorAtBeginning("Missing expected number")
    }
  }


  override def readBooleanLiteral(): Boolean = {
    stateMachine.literal()
    walker.skipSpaces()
    if(walker.tryRead(TRUE)) {
      true
    } else if(walker.tryRead(FALSE)) {
      false
    } else {
      throw walker.lexicalErrorAtBeginning("Missing expected boolean value")
    }
  }


  override def getCurrentLocation: CodeLocation = walker.getCurrentLocation
}
