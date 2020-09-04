package net.kfoundation.scala.serialization

import net.kfoundation.scala.io.Path
import net.kfoundation.scala.parse.CodeLocation
import net.kfoundation.scala.parse.lex._
import net.kfoundation.scala.parse.syntax.SyntaxError
import net.kfoundation.scala.serialization.K4ObjectDeserializer._
import net.kfoundation.scala.util.SimpleStack
import net.kfoundation.scala.{UChar, UString}



object K4ObjectDeserializer {
  private object State extends Enumeration {
    val STREAM_BEGIN, STREAM_END, OBJECT_BEGIN, OBJECT_END, COLLECTION_BEGIN,
      COLLECTION_END, PROPERTY, LITERAL = Value
  }

  private val OPEN_BRACE: UChar = '['
  private val CLOSE_BRACE: UChar = ']'
  private val OPEN_CURLY_BRACE: UChar = '{'
  private val CLOSE_CURLY_BRACE: UChar = '}'
  private val EQUAL: UChar = '='
  private val TRUE: UString = "true"
  private val FALSE: UString = "false"
  private val COLLECTION_STACK_SYMBOL: UString = "{}"
}



class K4ObjectDeserializer private (walker: CodeWalker)
  extends ObjectDeserializer
{
  import ObjectDeserializer._

  private val stack = new SimpleStack[UString]
  private var state = State.STREAM_BEGIN


  def this(path: Path) = this(new CodeWalker(
    path.getFileName.getOrElse("<bad-path-name>"),
    path.getInputStream))


  def this(s: UString) = this(CodeWalker.of(s))


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


  override def readObjectBegin(): ObjectBeginToken = {
    validateTransition(State.OBJECT_BEGIN)
    walker.skipSpaces()

    val token = IdentifierToken.reader
      .tryRead(walker)
      .getOrElse(throw walker.lexicalErrorAtBeginning(
        "Missing expected identifier"))

    walker.readSpaces()
    readOrError(OPEN_BRACE)
    stack.push(token.value)
    new ObjectBeginToken(token, Some(token.value))
  }


  override def readObjectEnd(): ObjectEndToken = {
    validateTransition(State.OBJECT_END)
    walker.skipSpaces()
    readOrError(CLOSE_BRACE)
    val name = stack.pop()

    if(name.isEmpty || name.get.equals(COLLECTION_STACK_SYMBOL)) {
      throw new SyntaxError("End object token at "
        + walker.getCurrentLocation.getLocationTag
        + " does not correspond to the beginning of any object.")
    }

    new ObjectEndToken(walker.commit(), name)
  }


  override def readCollectionBegin(): CollectionBeginToken = {
    validateTransition(State.COLLECTION_BEGIN)
    walker.skipSpaces()
    readOrError(OPEN_CURLY_BRACE)
    stack.push(COLLECTION_STACK_SYMBOL)
    new CollectionBeginToken(walker.commit())
  }


  override def tryReadCollectionEnd(): Option[CollectionEndToken] = {
    walker.skipSpaces()
    if(!walker.tryRead(CLOSE_CURLY_BRACE)) {
      None
    } else {
      validateTransition(State.COLLECTION_END)

      val stackTail = stack.pop()

      if(stackTail.isEmpty || !stackTail.get.equals(COLLECTION_STACK_SYMBOL)) {
        throw new SyntaxError("End collection token at"
          + walker.getCurrentLocation
          + " does not correspond to any object.")
      }
      Some(new CollectionEndToken(walker.commit()))
    }
  }


  override def tryReadPropertyName(): Option[PropertyNameToken] = {
    walker.skipSpaces()

    IdentifierToken.reader.tryRead(walker).map(id => {
      walker.readSpaces()
      readOrError(EQUAL)
      validateTransition(State.PROPERTY)
      new PropertyNameToken(id, id.value)
    })
  }


  override def readStringLiteral(): StringToken = {
    validateTransition(State.LITERAL)
    walker.skipSpaces()
    StringToken.reader
      .tryRead(walker)
      .getOrElse(throw walker.lexicalErrorAtBeginning("Missing string literal"))
  }


  override def readIntegerLiteral(): IntegralToken = {
    validateTransition(State.LITERAL)
    walker.skipSpaces()
    NumericToken.reader.tryRead(walker) match {
      case Some(i: IntegralToken) => i
      case Some(d: DecimalToken) => throw walker.lexicalErrorAtBeginning(
        "Expected an integer, found: " + d.value)
      case _ => throw walker.lexicalErrorAtBeginning("Missing expected integer")
    }
  }


  override def readDecimalLiteral(): DecimalToken = {
    validateTransition(State.LITERAL)
    walker.skipSpaces()
    NumericToken.reader.tryRead(walker) match {
      case Some(i: IntegralToken) => i.asDecimalToken
      case Some(d: DecimalToken) => d
      case _ => throw walker.lexicalErrorAtBeginning("Missing expected number")
    }
  }


  override def readBooleanLiteral(): BooleanToken = {
    validateTransition(State.LITERAL)
    walker.skipSpaces()
    if(walker.tryRead(TRUE)) {
      new BooleanToken(walker.commit(), true)
    } else if(walker.tryRead(FALSE)) {
      new BooleanToken(walker.commit(), false)
    } else {
      throw walker.lexicalErrorAtBeginning("Missing expected boolean value")
    }
  }

  override def getCurrentLocation: CodeLocation = walker.getCurrentLocation
}
