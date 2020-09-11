package net.kfoundation.scala.serialization

import java.io.{ByteArrayOutputStream, InputStream}

import net.kfoundation.scala.UString
import net.kfoundation.scala.encoding.XmlEscape
import net.kfoundation.scala.io.Path
import net.kfoundation.scala.parse.CodeLocation
import net.kfoundation.scala.parse.lex._
import net.kfoundation.scala.serialization.internals.ObjectStreamStateMachine

import scala.annotation.tailrec



object XmlObjectDeserializer {
  class MetaData(val version: UString, val encoding: UString)

  val FACTORY: ObjectDeserializerFactory = new ObjectDeserializerFactory {
    override def of(str: UString): ObjectDeserializer =
      new XmlObjectDeserializer(CodeWalker.of(str))

    override def of(input: InputStream): ObjectDeserializer =
      new XmlObjectDeserializer(CodeWalker.of(input))

    override def of(path: Path): ObjectDeserializer
    = new XmlObjectDeserializer(CodeWalker.of(path))
  }
}



class XmlObjectDeserializer private (walker: CodeWalker) extends ObjectDeserializer {
  import XmlObjectDeserializer._
  import internals.CommonSymbols._
  import internals.XmlSymbols._


  private val stateMachine = new ObjectStreamStateMachine
  private var isInsideTag = false
  private var currentPropertyName: Option[UString] = None


  private def tryReadPrologBeing(): Boolean = walker.tryRead(PROLOG_BEGIN)


  private def tryReadPrologEnd(): Boolean = walker.tryRead(PROLOG_END)


  private def tryReadTagBegin(): Option[UString] =
    if(walker.tryRead(LT)) {
      if(walker.tryReadAll(_ != SPACE) == 0) {
        throw walker.lexicalErrorAtBeginning("tag name expected")
      }
      val value = walker.getCurrentSelection
      walker.commit()
      Some(value)
    } else {
      None
    }


  private def tryReadEmptyTagEnd(): Boolean = {
    walker.skipSpaces()
    if(walker.tryRead(SLASH_GT)) {
      walker.commit()
      true
    } else {
      false
    }
  }


  private def tryReadTagEnd(): Boolean = walker.tryRead(GT)


  private def readAttribName(): UString = IdentifierToken.reader
    .tryRead(walker)
    .map(n => {
      if(!walker.tryRead(EQ)) {
        throw walker.lexicalErrorAtBeginning("'=' expected")
      }
      n.value
    })
    .getOrElse(throw walker.lexicalErrorAtBeginning("attribute name expected"))


  private def readAttribValue(): UString = StringToken.reader
    .tryRead(walker)
    .getOrElse(throw walker.lexicalErrorAtBeginning("bad or missing attribute value"))
    .value


  private def tryReadEscapeSequence: Option[UString] =
    if(walker.tryRead(AMP)) {
      if(walker.tryReadAll(_ != SEMICOLON.codePoint) == 0) {
        throw walker.lexicalErrorAtCurrentLocation("Escape sequence expected")
      }
      val unescaped = XmlEscape.unescapeOne(walker.getCurrentSelection)
      if(!walker.tryRead(SEMICOLON)) {
        throw walker.lexicalErrorAtCurrentLocation("';' expected")
      }
      Some(unescaped)
    } else {
      None
    }


  private def readText(): UString = {
    val begin = walker.getCurrentLocation
    var end = begin
    val buffer = new ByteArrayOutputStream()

    var hasMore = true
    while(hasMore) {
      skipComments()
      val nRead = walker.tryReadAll(cp => cp != AMP_CP && cp != TAG_BEGIN_CP)
      if(nRead > 0)  {
        buffer.write(walker.getCurrentSelection.toUtf8, 0, nRead)
        end = walker.commit().end
        hasMore = true
      }
      tryReadEscapeSequence.foreach(part => {
        buffer.write(part.toUtf8)
        hasMore = true
      })
    }

    UString.of(buffer.toByteArray)
  }


  private def tryReadClosingTag(): Option[UString] =
    if(!walker.tryRead(LT_SLASH)) {
      None
    } else {
      IdentifierToken.reader.tryRead(walker).map(v => {
        if(!walker.tryRead(GT)) {
          throw walker.lexicalErrorAtBeginning("'>' expected")
        }
        v.value
      })
    }


  @tailrec
  private def skipComments(): Unit =
    if(walker.tryRead(COMMENT_BEGIN)) {
      walker.skipAll(_ != TAG_BEGIN_CP)
      if(!walker.tryRead(COMMENT_END)) {
        walker.tryRead(LT)
        skipComments()
      }
    }


  private def skipCommentsAndSpaces(): Unit = {
    walker.skipSpaces()
    skipComments()
    walker.skipSpaces()
  }


  def tryReadProlog(): Option[MetaData] =
    if(tryReadPrologBeing()) {
      var version: Option[UString] = None
      var encoding: Option[UString] = None

      skipCommentsAndSpaces()
      while(!tryReadPrologEnd()) {
        val name = readAttribName()
        val value = readAttribValue()
        name match {
          case VERSION => version = Some(value)
          case ENCODING => encoding = Some(value)
        }
      }

      Some(new MetaData(
        version.getOrElse(UString.EMPTY),
        encoding.getOrElse(UString.EMPTY)))
    } else {
      None
    }


  override def readObjectBegin(): Option[UString] = {
    import ObjectStreamStateMachine.State._

    if(stateMachine.getState == STREAM_BEGIN) {
      tryReadProlog()
    }

    if(currentPropertyName.isDefined) {
      if(isInsideTag) {
        throw walker.lexicalErrorAtBeginning("Illegal attempt to read an object as value of an attribute")
      }
      stateMachine.objectBegin(currentPropertyName.get)
      currentPropertyName = None
      None
    } else {
      val name = tryReadTagBegin().getOrElse(
        throw walker.lexicalErrorAtBeginning("Expected XML tag"))
      isInsideTag = true
      stateMachine.objectBegin(name)
      Some(name)
    }
  }


  override def readObjectEnd(): Option[UString] =
    if(isInsideTag) {
      isInsideTag = false
      if(tryReadEmptyTagEnd()) {
        stateMachine.objectEnd()
      } else if(tryReadTagEnd()){
        Some(readObjectEndByClosingTag())
      } else {
        throw walker.lexicalErrorAtBeginning("'>' expected")
      }
    } else {
      Some(readObjectEndByClosingTag())
    }


  private def readObjectEndByClosingTag(): UString = {
    val name = tryReadClosingTag().getOrElse(
      throw walker.lexicalErrorAtBeginning("Expected XML closing tag"))

    val expectedName = stateMachine.objectEnd().getOrElse(
      throw new RuntimeException("State machine does not contain tag name. This should not have happened."))

    if(!name.equals(expectedName)) {
      throw walker.lexicalErrorAtBeginning("Expected end tag for \""
        + expectedName + "\" but found: " + name)
    }

    name
  }


  override def readCollectionBegin(): Unit =
    currentPropertyName.map(n => stateMachine.collectionBegin(n))
      .getOrElse(throw walker.lexicalErrorAtBeginning(
        "Illegal attempt reading a collection that does not follow a property definition"))


  override def tryReadCollectionEnd(): Boolean =
    tryReadClosingTag().exists(tagName => {
      val expected = stateMachine.collectionEnd()
        .getOrElse(throw new RuntimeException(
          "State machine does not contain tag name for collection. This should not have happened."))

      if (!tagName.equals(expected)) {
        throw walker.lexicalErrorAtBeginning("Expected closing tag for \""
          + expected + "\" but found: " + tagName)
      }

      true
    })


  override def readStringLiteral(): UString =
    if(isInsideTag) {
      readAttribValue()
    } else {
      readText()
    }


  override def readIntegerLiteral(): Long = {
    val str = readStringLiteral()
    try {
      str.toLong
    } catch {
      case _: NumberFormatException =>
        throw walker.lexicalErrorAtBeginning("Expected an integer but found: " + str)
    }
  }


  override def readDecimalLiteral(): Double = {
    val str = readStringLiteral()
    try {
      str.toDouble
    } catch {
      case _: NumberFormatException =>
        throw walker.lexicalErrorAtBeginning("Expected a decimal number but found: " + str)
    }
  }


  override def readBooleanLiteral(): Boolean = readStringLiteral() match {
    case TRUE => true
    case FALSE => false
    case x => throw walker.lexicalErrorAtBeginning("Expected a boolean value but found: " + x)
  }


  override def tryReadPropertyName(): Option[UString] = if(isInsideTag) {
    if(tryReadTagEnd()) {
      isInsideTag = false
      tryReadTagBegin()
    } else {
      Some(readAttribName())
    }
  } else {
    tryReadTagBegin()
  }


  override protected def getCurrentLocation: CodeLocation =
    walker.getCurrentLocation
}