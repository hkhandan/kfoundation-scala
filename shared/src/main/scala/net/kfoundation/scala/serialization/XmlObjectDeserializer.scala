// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.serialization

import java.io.{ByteArrayOutputStream, InputStream}

import net.kfoundation.scala.UString
import net.kfoundation.scala.encoding.XmlEscape
import net.kfoundation.scala.io.Path
import net.kfoundation.scala.parse.CodeLocation
import net.kfoundation.scala.parse.lex._
import net.kfoundation.scala.serialization.internals.CommonSymbols._
import net.kfoundation.scala.serialization.internals.ObjectStreamStateMachine
import net.kfoundation.scala.serialization.internals.XmlSymbols._

import scala.annotation.tailrec


/** XML object deserializer */
object XmlObjectDeserializer {

  class MetaData(val version: UString, val encoding: UString)

  private class AttributeReader() {
    private var _names: Seq[UString] = Seq()
    private var _values: Seq[Option[UString]] = Seq()

    def this(walker: CodeWalker) = {
      this()

      var hasMore = true

      while(hasMore) {
        walker.skipSpaces()
        hasMore = false

        IdentifierToken.reader
          .tryRead(walker)
          .foreach(id => {
            _names = _names :+ id.value

            walker.skipSpaces()
            if (!walker.tryRead(EQ)) {
              throw walker.lexicalErrorAtCurrentLocation("'=' expected")
            }

            walker.skipSpaces()
            if(walker.tryRead(NULL)) {
              _values = _values :+ None
            } else {
              _values = _values :+ StringToken.reader
                .tryRead(walker)
                .map(_.value)
                .orElse(
                  throw walker.lexicalErrorAtCurrentLocation("Attribute value expected"))
            }

            hasMore = true
          })
      } // while(hasMore)
    }

    def hasMore: Boolean = _values.nonEmpty

    def nextName(): UString = {
      val name = _names.head
      _names = _names.tail
      name
    }

    def nextValue(): UString = {
      val value = _values.head
      _values = _values.tail
      value.orNull
    }

    def isNextValueNull: Boolean = _values.head.isEmpty

    def get(name: UString): Option[UString] = {
      val l = _names.indexOf(name)
      if(l > 0) {
        _values(l)
      } else {
        None
      }
    }
  }

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


  private val stateMachine = new ObjectStreamStateMachine
  private var currentPropertyName: Option[UString] = None
  private var attributes: AttributeReader = new AttributeReader()
  private var isEmptyTag: Boolean = false
  private var endTag: Option[UString] = None


  private def tryReadTagBegin(): Option[UString] =
    if(walker.tryRead(LT)) {
      walker.commit()
      Some(IdentifierToken.reader
        .tryRead(walker)
        .getOrElse(
          throw walker.lexicalErrorAtBeginning("tag name expected, " + stateMachine))
        .value)
    } else {
      None
    }


  private def tryReadEmptyTagEnd(): Boolean =
    if(walker.tryRead(SLASH_GT)) {
      walker.commit()
      true
    } else {
      false
    }


  private def tryReadTagEnd(): Boolean =
    if(walker.tryRead(GT)) {
      walker.commit()
      true
    } else {
      false
    }


  private def tryReadEscapeSequence: Option[UString] =
    if(walker.tryRead(AMP)) {
      if(walker.readAll(_ != SEMICOLON.codePoint) == 0) {
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
      hasMore = false
      skipComments()
      val nRead = walker.readAll(cp => cp != AMP_CP && cp != TAG_BEGIN_CP)
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
      walker.commit()
      IdentifierToken.reader
        .tryRead(walker)
        .map(v => {
            if(!walker.tryRead(GT)) {
              throw walker.lexicalErrorAtBeginning("'>' expected")
            }
            walker.commit()
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


  def tryReadProlog(): Option[MetaData] = {
    skipCommentsAndSpaces()
    if(walker.tryRead(PROLOG_BEGIN)) {
      val attribReader = new AttributeReader(walker)
      walker.skipSpaces()
      if(!walker.tryRead(PROLOG_END)) {
        throw walker.lexicalErrorAtCurrentLocation(s"'$PROLOG_END' expected")
      }
      walker.commit()
      Some(new MetaData(
        attribReader.get(VERSION).getOrElse(""),
        attribReader.get(ENCODING).getOrElse("")))
    } else {
      None
    }
  }


  override def readObjectBegin(): Option[UString] = {
    import ObjectStreamStateMachine.State._

    if(stateMachine.getState == STREAM_BEGIN) {
      tryReadProlog()
    }

    if(currentPropertyName.isDefined) {
      stateMachine.objectBegin(currentPropertyName.get)
      currentPropertyName = None
      None
    } else {
      walker.skipSpaces()
      val name = tryReadTagBegin().getOrElse(
        throw walker.lexicalErrorAtBeginning("XML opening tag expected"))
      stateMachine.objectBegin(name)
      attributes = new AttributeReader(walker)
      walker.skipSpaces()
      isEmptyTag = tryReadEmptyTagEnd()
      if(!isEmptyTag && !tryReadTagEnd()) {
        throw walker.lexicalErrorAtCurrentLocation(s"'$GT' or '$SLASH_GT' expected")
      }
      Some(name)
    }
  }


  override def readObjectEnd(): Option[UString] =
    if(isEmptyTag) {
      stateMachine.objectEnd()
    } else if(endTag.isDefined) {
      stateMachine.objectEnd(endTag.get)
      val t = endTag
      endTag = None
      t
    } else {
      val maybeName = tryReadClosingTag()
      if(maybeName.isEmpty) {
        throw walker.lexicalErrorAtCurrentLocation("XML closing tag expected for: " + stateMachine.peek)
      }
      stateMachine.objectEnd(maybeName.get)
      maybeName
    }


  override def readCollectionBegin(): Unit =
    currentPropertyName.map(n => {
      stateMachine.collectionBegin(n)
      currentPropertyName = None
    })
    .getOrElse(throw walker.lexicalErrorAtBeginning(
      "Illegal attempt reading a collection that does not follow a property definition"))


  override def tryReadCollectionEnd(): Boolean = {
    walker.skipSpaces()
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
  }


  override def tryReadPropertyName(): Option[UString] = {
    if(attributes.hasMore) {
      stateMachine.property()
      currentPropertyName = None
      Some(attributes.nextName())
    } else {
      walker.skipSpaces()
      endTag = tryReadClosingTag()

      if(endTag.isDefined) {
        None
      } else {
        walker.skipSpaces()

        currentPropertyName = tryReadTagBegin()
        if (currentPropertyName.isDefined) {
          stateMachine.property()
        }

        isEmptyTag = false

        if (!tryReadTagEnd()) {
          if (!tryReadEmptyTagEnd()) {
            throw walker.lexicalErrorAtCurrentLocation("'>' or '/>' expected")
          } else {
            isEmptyTag = true
          }
        }

        currentPropertyName
      }
    }
  }


  override def tryReadNullLiteral(): Boolean =
    if(attributes.hasMore) {
      if(attributes.isNextValueNull) {
        stateMachine.literal()
        attributes.nextValue()
        true
      } else {
        false
      }
    } else {
      isEmptyTag
    }


  override def readStringLiteral(): UString = {
    stateMachine.literal()
    if(attributes.hasMore) {
      attributes.nextValue()
    } else {
      val value = readText()
      val propName = currentPropertyName.getOrElse(
        new RuntimeException("Property name is missing. This should not have happened."))
      val name = tryReadClosingTag().getOrElse(
        throw walker.lexicalErrorAtCurrentLocation("XML closing tag expected for element: " + propName))
      if(!name.equals(propName)) {
        throw walker.lexicalErrorAtCurrentLocation(
          "Closing tag for \"" + propName + "\" expected but found: " + name)
      }
      currentPropertyName = None
      value
    }
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


  override protected def getCurrentLocation: CodeLocation =
    walker.getCurrentLocation
}