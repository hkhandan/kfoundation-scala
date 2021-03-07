// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui.render

import net.kfoundation.scala.UString
import net.kfoundation.scala.io.{Path, URL}
import net.kfoundation.scala.parse.lex.CodeWalker
import net.kfoundation.scala.util.Flow
import net.kfoundation.scala.uui._


/** Renders a Document into plain text. */
object PlainTextRenderer {
  import UString._

  // CONTENT //

  private def text(b: Builder, t: Content.Text): Unit =
    b.append(t.value)


  private def link(b: Builder, href: URL, text: UString): Unit = {
    val linkStr = href.toUString
    if(linkStr == text) {
      b.appendAll("[", linkStr, "]")
    } else {
      b.appendAll('[', text, " | ", href.toUString, ']')
    }
  }


  private def link(b: Builder, t: Content.Link): Unit = link(b, t.href, t.text)


  private def cellSeq(b: Builder, cells: Seq[Content], begin: UString,
    end: UString, delimiter: UString): Unit =
  {
    b.append(begin)
    val head = cells.headOption
    head.foreach(c => content(b, c))
    if(head.nonEmpty) {
      cells.tail.foreach(c => {
        b.append(delimiter)
        content(b, c)
      })
    }
    b.append(end)
  }


  private def vBand(b: Builder, vb: Content.VerticalBand): Unit =
    cellSeq(b, vb.cells.map(_.content), "", "", "\n\n")


  private def hBand(b: Builder, hb: Content.HorizontalBand): Unit =
    cellSeq(b, hb.cells.map(_.content), "[", "]", " | ")


  private def hFlow(b: Builder, f: Content.HorizontalFlow): Unit =
    cellSeq(b, f.contents, "[", "]", " | ")


  private def vFlow(b: Builder, f: Content.VerticalFlow): Unit =
    cellSeq(b, f.contents, "", "", "\n\n")


  private def free(b: Builder, f: Content.Free): Unit =
    cellSeq(b, f.contents, "", "", "\n\n---------\n\n")


  private def composed(b: Builder, c: Content.Composite): Unit =
    content(b, c.apply())


  private def dynamic(b: Builder, d: Content.Dynamic): Unit =
    d.apply().peek.foreach(content(b, _))


  private def styled(b: Builder, s: Content.Styled): Unit = content(b, s.content)


  private def grid(b: Builder, g: Content.Grid): Unit =
    g.contents.foreach(c => {
      b.append("( ")
      content(b, c)
      b.append(" )")
    })


  private def list(b: Builder, style: ParagraphStyle, l: Content.List): Unit =
    l.items.foreach(i => {
      b.append(" * ")
      typesetItem(b, style, i)
    })


  private def lotex(b: Builder, dom: LoTexDocumentModel, styles: ParagraphStyle): Unit=
    dom.parts.foreach(p => {
      p match {
        case l: LoTexDocumentModel.LinkedText =>
          link(b, l.href, l.text)
        case t: LoTexDocumentModel.FormattedText =>
          b.append(t.text)
      }
    })


  private def paragraph(b: Builder, style: ParagraphStyle,
  p: Content.Paragraph): Unit =
    lotex(b, LoTexDocumentModel.PARSER
      .tryRead(CodeWalker.of(p.text)).get, style)


  private def typesetItem(b: Builder, style: ParagraphStyle,
  i: Content.TypesetItem): Unit = i match {
    case l: Content.List => list(b, style, l)
    case p: Content.Paragraph => paragraph(b, style, p)
  }


  private def typeset(b: Builder, t: Content.Typeset): Unit = {
    val head = t.content.headOption
    head.foreach(typesetItem(b, t.style, _))
    if(head.nonEmpty) {
      t.content.tail.foreach(i => {
        b.append("\n\n")
        typesetItem(b, t.style, i)
      })
    }
  }


  private def content(b: Builder, c: Content): Unit = c match {
    case t: Content.Text => text(b, t)
    case l: Content.Link => link(b, l)
    case t: Content.Typeset => typeset(b, t)
    case g: Content.Grid => grid(b, g)
    case hb: Content.HorizontalBand => hBand(b, hb)
    case vb: Content.VerticalBand => vBand(b, vb)
    case f: Content.VerticalFlow => vFlow(b, f)
    case f: Content.HorizontalFlow => hFlow(b, f)
    case f: Content.Free => free(b, f)
    case cc: Content.Composite => composed(b, cc)
    case d: Content.Dynamic => dynamic(b, d)
    case s: Content.Styled => styled(b, s)
    case r: Content.WritingDirection => content(b, r.content)
    case _: Content.Rect => /* Nothing */
    case _: Content.Empty => /* Nothing */
    case _ => throw new RuntimeException("Unsupported content: " + c)
  }


  def toString(d: Document): UString = {
    val dir = Path(System.getProperty("user.dir"))
    val url = URL("file", URL.Authority(Seq(U"localhost")), dir)
    val traits = new MediaTraits(false, false, GraphicsMode.TEXT,
      ColorMode.MONOCHROME, None, None, url)
    val state = Flow.closed[UString]
    val b = UString.builder
    d.apply(traits, state).peek.foreach(c => content(b, c))
    b.build
  }
}
