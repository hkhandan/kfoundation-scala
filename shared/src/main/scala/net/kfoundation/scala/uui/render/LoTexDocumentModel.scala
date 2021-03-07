// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui.render

import net.kfoundation.scala.{UObject, UString}
import net.kfoundation.scala.UString.Interpolator
import net.kfoundation.scala.io.URL
import net.kfoundation.scala.parse.{ParseError, Parser}
import net.kfoundation.scala.parse.lex.CodeWalker
import net.kfoundation.scala.uui.ParagraphStyle
import net.kfoundation.scala.uui.render.LoTexDocumentModel.FormattedText


object LoTexDocumentModel {
  import ParseError._


  class FormattedText(val text: UString, val formatName: UString)
    extends UObject
  {
    override def appendTo(builder: UString.Builder): Unit =
      if(formatName == ParagraphStyle.NORMAL) builder.append(text)
      else builder.appendAll('\\', formatName, '{', text, '}')
  }


  class LinkedText(text: UString, format: UString, val href: URL)
    extends FormattedText(text, format)
  {
    override def appendTo(builder: UString.Builder): Unit =
      builder.appendAll('\\', LINK, '{', href, '}', '{', text, '}')
  }

  private val BACKSLASH = '\\'
  private val OPEN_CBRACE = '{'
  private val CLOSE_CBRACE = '}'
  private val LINK = U"link"
  private val IDENTIFIER = U"IDENTIFIER"

  val PARSER: Parser[LoTexDocumentModel] = new Parser[LoTexDocumentModel] {
    private def wrap(c: Char): UString = U"'$c'"

    private def readLinkPart(w: CodeWalker): LinkedText = {
      if(!w.tryRead(OPEN_CBRACE)) {
        throw w.parseError(MISSING_TOKEN, SHOULD -> wrap(OPEN_CBRACE))
      }
      w.commit()
      w.readAll(_ != CLOSE_CBRACE)
      val linkOrText = w.getCurrentSelection
      if(linkOrText.isEmpty) {
        throw w.parseError(MISSING_TOKEN, SHOULD -> "TEXT_OR_LINK")
      }
      w.commit()
      if(!w.tryRead(CLOSE_CBRACE)) {
        throw w.parseError(MISSING_TOKEN, SHOULD -> wrap(CLOSE_CBRACE))
      }
      w.commit()

      if(w.tryRead(OPEN_CBRACE)) {
        w.commit()
        w.readAll(_ != CLOSE_CBRACE)
        val text = w.getCurrentSelection
        if(text.isEmpty) {
          throw w.parseError(MISSING_TOKEN, SHOULD -> "TEXT")
        }
        w.commit()
        if(!w.tryRead(CLOSE_CBRACE)) {
          throw w.parseError(MISSING_TOKEN, SHOULD -> wrap(CLOSE_CBRACE))
        }
        w.commit()
        new LinkedText(text, LINK, URL(linkOrText))
      } else {
        new LinkedText(linkOrText, LINK, URL(linkOrText))
      }
    }

    private def readOtherPart(tag: UString, w: CodeWalker): FormattedText = {
      if(!w.tryRead(OPEN_CBRACE)) {
        throw w.parseError(MISSING_TOKEN, SHOULD -> wrap(OPEN_CBRACE))
      }
      w.commit()
      w.readAll(_ != CLOSE_CBRACE)
      val text = w.getCurrentSelection
      if(!w.tryRead(CLOSE_CBRACE)) {
        throw w.parseError(MISSING_TOKEN, SHOULD -> wrap(CLOSE_CBRACE))
      }
      w.commit()
      new FormattedText(text, tag)
    }

    private def readFormattedPart(w: CodeWalker): FormattedText = {
      w.readAll(_ != OPEN_CBRACE)
      val rawTag = w.getCurrentSelection
      val tag = if(rawTag.isEmpty) ParagraphStyle.NORMAL else rawTag
      w.commit()
      if(tag.equals(LINK)) {
        readLinkPart(w)
      } else {
        readOtherPart(tag, w)
      }
    }

    private def readPart(w: CodeWalker): Option[FormattedText] =
      if(w.tryRead(BACKSLASH)) {
        w.commit()
        Some(readFormattedPart(w))
      } else {
        w.readAll(_ != BACKSLASH)
        val s = w.getCurrentSelection
        w.commit()
        if(s.isEmpty) {
          None
        } else {
          Some(new FormattedText(s, ParagraphStyle.NORMAL))
        }
      }

    override def tryRead(w: CodeWalker): Option[LoTexDocumentModel] = {
      var parts: Seq[FormattedText] = Nil
      var hasMore = true
      while(hasMore) {
        val part = readPart(w)
        parts = parts.appendedAll(part)
        hasMore = part.nonEmpty
      }
      Some(new LoTexDocumentModel(parts))
    }
  }
}


class LoTexDocumentModel(val parts: Seq[FormattedText]) extends UObject {
  override def appendTo(builder: UString.Builder): Unit =
    builder.appendJoining(parts, "")
}