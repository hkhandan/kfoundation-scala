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

import net.kfoundation.scala.io.Path
import net.kfoundation.scala.parse.CodeLocation
import net.kfoundation.scala.parse.lex._
import net.kfoundation.scala.parse.syntax.SyntaxError
import net.kfoundation.scala.serialization.K4ObjectDeserializer._
import net.kfoundation.scala.util.SimpleStack
import net.kfoundation.scala.{UChar, UString}



/**  */
object K4ObjectDeserializer {
  private val COLLECTION_STACK_SYMBOL: UString = "*"

  val FACTORY: ObjectDeserializerFactory = new ObjectDeserializerFactory {
    override def of(str: UString): ObjectDeserializer =
      new K4ObjectDeserializer(CodeWalker.of(str))

    override def of(input: InputStream): ObjectDeserializer =
      new K4ObjectDeserializer(CodeWalker.of(input))

    override def of(path: Path): ObjectDeserializer
    = new K4ObjectDeserializer(CodeWalker.of(path))
  }
}



/** K4 object deserializer */
class K4ObjectDeserializer private (walker: CodeWalker)
  extends ObjectDeserializer
{
  import internals.ObjectStreamStateMachine.State
  import internals.CommonSymbols._

  private val stack = new SimpleStack[UString]
  private var state = State.STREAM_BEGIN


  private def readOrError(symbol: UChar): Unit = {
    if(!walker.tryRead(symbol)) {
      throw walker.lexicalErrorAtCurrentLocation(
        "Missing expected symbol '" + symbol.toString + "'")
    }
  }


  private def validateTransition(s: State.Value): Unit = {
    import State._

    val isAllowed: Boolean = state match {
      case STREAM_BEGIN => s == OBJECT_BEGIN
      case STREAM_END => false
      case OBJECT_BEGIN => s == PROPERTY || s == OBJECT_END
      case OBJECT_END => s == PROPERTY || s == OBJECT_END || s == COLLECTION_END
      case COLLECTION_BEGIN => s == OBJECT_BEGIN || s == COLLECTION_END
      case COLLECTION_END => s == PROPERTY || s == OBJECT_END
      case PROPERTY => s == LITERAL || s == OBJECT_BEGIN || s == COLLECTION_BEGIN
      case LITERAL => s == PROPERTY || s == OBJECT_END
    }

    if(!isAllowed) {
      throw new DeserializationError(s"Invalid attempt to transition from $state to $s")
    }

    state = s
  }


  override def readObjectBegin(): Option[UString] = {
    validateTransition(State.OBJECT_BEGIN)
    walker.skipSpaces()

    val token = IdentifierToken.reader
      .tryRead(walker)
      .getOrElse(throw walker.lexicalErrorAtBeginning(
        "Missing expected identifier"))

    walker.readSpaces()
    readOrError(OPEN_BRACE)
    stack.push(token.value)
    Some(token.value)
  }


  override def readObjectEnd(): Option[UString] = {
    validateTransition(State.OBJECT_END)
    walker.skipSpaces()
    readOrError(CLOSE_BRACE)
    val name = stack.pop()

    if(name.isEmpty || name.get.equals(COLLECTION_STACK_SYMBOL)) {
      throw new SyntaxError("End object token at "
        + walker.getCurrentLocation.getLocationTag
        + " does not correspond to the beginning of any object.")
    }

    walker.commit()
    name
  }


  override def readCollectionBegin(): Unit = {
    validateTransition(State.COLLECTION_BEGIN)
    walker.skipSpaces()
    readOrError(OPEN_CURLY_BRACE)
    stack.push(COLLECTION_STACK_SYMBOL)
    walker.commit()
  }


  override def tryReadCollectionEnd(): Boolean = {
    walker.skipSpaces()
    if(!walker.tryRead(CLOSE_CURLY_BRACE)) {
      false
    } else {
      validateTransition(State.COLLECTION_END)

      val stackTail = stack.pop()

      if(stackTail.isEmpty || !stackTail.get.equals(COLLECTION_STACK_SYMBOL)) {
        throw new SyntaxError("End collection token at"
          + walker.getCurrentLocation
          + " does not correspond to any object.")
      }
      true
    }
  }


  override def tryReadPropertyName(): Option[UString] = {
    walker.skipSpaces()

    IdentifierToken.reader.tryRead(walker).map(id => {
      walker.readSpaces()
      readOrError(EQUAL)
      validateTransition(State.PROPERTY)
      id.value
    })
  }


  override def readStringLiteral(): UString = {
    validateTransition(State.LITERAL)
    walker.skipSpaces()
    StringToken.reader
      .tryRead(walker)
      .getOrElse(throw walker.lexicalErrorAtBeginning("Missing string literal"))
      .value
  }


  override def readIntegerLiteral(): Long = {
    validateTransition(State.LITERAL)
    walker.skipSpaces()
    NumericToken.reader.tryRead(walker) match {
      case Some(i: IntegralToken) => i.value
      case Some(d: DecimalToken) => throw walker.lexicalErrorAtBeginning(
        "Expected an integer, found: " + d.value)
      case _ => throw walker.lexicalErrorAtBeginning("Missing expected integer")
    }
  }


  override def readDecimalLiteral(): Double = {
    validateTransition(State.LITERAL)
    walker.skipSpaces()
    NumericToken.reader.tryRead(walker) match {
      case Some(i: IntegralToken) => i.asDecimalToken.value
      case Some(d: DecimalToken) => d.value
      case _ => throw walker.lexicalErrorAtBeginning("Missing expected number")
    }
  }


  override def readBooleanLiteral(): Boolean = {
    validateTransition(State.LITERAL)
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
