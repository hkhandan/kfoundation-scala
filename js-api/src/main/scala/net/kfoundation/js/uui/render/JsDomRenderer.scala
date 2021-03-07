package net.kfoundation.js.uui.render

import net.kfoundation.scala.UString.Interpolator
import net.kfoundation.scala.io.URL
import net.kfoundation.scala.util.Flow
import net.kfoundation.scala.uui.MouseEvents._
import net.kfoundation.scala.uui._
import net.kfoundation.scala.uui.render.IntermediateDomRenderer._
import net.kfoundation.scala.{UChar, UString}
import org.scalajs.dom
import org.scalajs.dom.UIEvent
import org.scalajs.dom.raw.HTMLInputElement

import java.io.{ByteArrayOutputStream, PrintStream}
import scala.scalajs.js.Date
import scala.util.Using


object JsDomRenderer {
  import net.kfoundation.scala.uui.DSL.lengthScalar

  private val COOKIE_NAME = "uuiAppState"

  private def mouseButton(n: Int): MouseButton = n match {
    case 1 => MouseButtons.MAIN
    case 2 => MouseButtons.SECONDARY
    case 3 => MouseButtons.AUXILIARY
    case _ => MouseButtons.OTHER
  }


  private def mouseEvent(e: dom.MouseEvent): MouseData =
    new MouseData(
      position = new Position(e.clientX.px, e.clientY.px),
      positionInFrame = new Position(e.pageX.px, e.pageY.px),
      positionOnScreen = new Position(e.screenX.px, e.screenY.px),
      button = mouseButton(e.button),
      key = new ModifierKeys(e.shiftKey, e.altKey, e.ctrlKey, e.metaKey))


  private def staticToJs(e: StaticElement): dom.Element = {
    val de = dom.document.createElement(e.name)

    e.attribs.items.foreach(a => de.setAttribute(a.name, a.value))

    e match {
      case ce: ContainerElement => ce.children.foreach(
        child => de.appendChild(toJs(child)))

      case te: TextElement => de.appendChild(
        dom.document.createTextNode(te.text))

      case ee: EmbeddedElement =>
        de.innerHTML = ee.html

      case _ => ()
    }

    val te = e.trimEvents.asInstanceOf[StaticElement]

    te.events.mouseEvents.foreach(v => {
      val writable = Flow.writable[MouseData]
      writable.forward(v.handler)
      de.addEventListener[dom.MouseEvent](v.name, data => {
        writable.write(mouseEvent(data))
      })
      // println(s"Event: ${v.name}, Trace: ${writable.trace}")
    })

    te.events.actionEvents.foreach(v => {
      val writable = Flow.writable[Unit]
      writable.forward(v.handler)
      de.addEventListener[dom.MouseEvent](v.name,
        _ => writable.write( () ))
      // println(s"Event: ${v.name}, Trace: ${writable.trace}")
    })

    te.events.textEvents.foreach(v => {
      val writable = Flow.writable[UString]
      writable.forward(v.handler)
      de.addEventListener[dom.UIEvent]("input",
        _ => writable.write(de.asInstanceOf[HTMLInputElement].value))
      // println(s"Event: ${v.name}, Trace: ${writable.trace}")
    })

    de
  }


  private def dynamicPlaceholder(name: UString): dom.Element = {
    val e = dom.document.createElement("span")
    e.setAttribute("id", name)
    e
  }

  private def renderError(th: Throwable): dom.Element = {
    val e = dom.document.createElement("div")
    e.setAttribute("style", "font-family: Courier; background-color: lightpink; " +
      "margin: 15px; padding: 4px; border: 1px solid brown; position: absolute")

    val str = Using.Manager(m => {
      val os = m(new ByteArrayOutputStream())
      val ps = m(new PrintStream(os))
      th.printStackTrace(ps)
      UString.of(os.toByteArray)
    }).get

    val builder = UString.builder
    builder.append("<div style='font-weight: bold; margin-bottom: 6px'>")
      .append(th.getMessage).append("</div>")
      .append("<div style='font-size: 8px'>")

    val nl: UChar = '\n'
    str.uCharIterator.foreach(ch =>
      if(ch == nl) builder.append("<br/>") else builder.append(ch))
    builder.append("</div>")

    e.innerHTML = builder.build
    e
  }


  private def dynamicToJs(de: DynamicElement): dom.Element =
    de.incarnations
      .map(toJs)
      .recover(renderError(_))
      .default(dynamicPlaceholder(de.name))
      .delta
      .foreach(d =>
        d.past.foreach(p => p.parentNode.replaceChild(d.present, p)))
      .peek.get
      .present


  private def toJs(e: Element): dom.Element = e match {
    case dynamic: DynamicElement => dynamicToJs(dynamic)
    case static: StaticElement => staticToJs(static)
    case _ => throw new IllegalArgumentException
  }


  def bodySize: Size = Size(
    Length.ofPixels(dom.document.body.clientWidth),
    Length.ofPixels(dom.document.body.clientHeight))


  def writeCookie(name: UString, value: UString): Unit = {
    val encoded = URL.encode(value)
    val d = new Date()
    d.setTime(d.getTime() + 30*24*60*60*1000.0)
    val expires = d.toUTCString()
    val cookie = U"$name=$encoded; path=/; expires=$expires"
    dom.document.cookie = cookie
  }


  def readCookie(name: String): Option[UString] = {
    val cookie = dom.document.cookie
    val cookies = cookie.split(";")
      .map(part => {
        val split = part.trim.split("=")
        if(split.length == 1) {
          (split(0), "")
        } else {
          (split(0), split(1))
        }
      })
      .toMap

    cookies.get(name)
      .map(value => URL.decode(UString.of(value)))
  }

  def setupUI(cookieName: UString, d: Document): Unit = {
    val clipboard = new MediaTraits.Clipboard {
      override def copy(text: UString): Unit =
        dom.window.navigator.clipboard.writeText(text.toString())
    }

    val navigator = new MediaTraits.Navigator {
      override def forward(url: URL): Unit =
        dom.window.location.assign(url.toString())
      override def open(url: URL): Unit = dom.window.open(url.toString())
    }

    dom.document.body.style = "margin: 0px; padding: 0px; height: 100%"
    val rawUrl = dom.document.documentURI
    val url = URL(rawUrl)

    val traits = new MediaTraits(false, true, GraphicsMode.RASTER,
      ColorMode.HD, Some(clipboard), Some(navigator), url)

    val size = Flow.writable[Size](bodySize)
    size.forward(d.size)

    val state = Flow.inlet[UString](readCookie(cookieName))
    state.foreach(writeCookie(cookieName, _))

    dom.document.body.innerHTML = ""
    dom.document.body.appendChild(toJs(document(d, traits, state)))

    if(d.size.isActive) {
      dom.window.addEventListener[UIEvent]("resize", _ => size.write(bodySize))
    }
  }
}
