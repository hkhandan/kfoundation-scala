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
import net.kfoundation.scala.UString.Interpolator
import net.kfoundation.scala.io.{Path, URL}
import net.kfoundation.scala.util.Flow
import net.kfoundation.scala.uui._

/** Renders a Document into a static HTML. */
object HtmlRenderer {
  import IntermediateDomRenderer._

  private val EMAIL_RENDERER = new IntermediateDomRenderer(true)

  private def writeOpenTag(s: UString.Builder, name: String, attribs: Attribs,
    noContent: Boolean) : Unit =
    s.append("<")
      .append(name)
      .append(if(attribs.items.isEmpty) "" else attribs.items.mkString(" ", " ", ""))
      .append(if(noContent) "/>" else ">")
      .append("\n")


  private def writeCloseTag(s: UString.Builder, name: String): Unit = {
    s.append("</").append(name).append(">\n")
  }


  private def build(e: Element, s: UString.Builder): Unit = e match {
    case t: TextElement =>
      writeOpenTag(s, t.name, t.attribs, false)
      s.appendReplacing(t.text, '\n', "<br/>").append("\n")
      writeCloseTag(s, t.name)

    case ce: ContainerElement =>
      writeOpenTag(s, ce.name, ce.attribs, false)
      ce.children.foreach(build(_, s))
      writeCloseTag(s, ce.name)

    case de: DynamicElement =>
      build(de.incarnations
          .peek
          .getOrElse(
            new TextElement("span", Attribs.EMPTY, new Events, "[Dynamic]")),
        s)

    case se: StaticElement =>
      writeOpenTag(s, se.name, se.attribs, true)

    case _ => throw new IllegalArgumentException
  }


  private def toString(e: Element): String = {
    val builder = UString.builder
    build(e, builder)
    builder.build
  }


  def toString(d: Document): String = {
    val dir = Path(System.getProperty("user.dir"))
    val url = URL("file", URL.Authority(Seq(U"localhost")), dir)
    val traits = new MediaTraits(false, false, GraphicsMode.RASTER,
      ColorMode.HD, None, None, url)
    val state = Flow.closed[UString]
    toString(EMAIL_RENDERER.document(d, traits, state))
  }
}