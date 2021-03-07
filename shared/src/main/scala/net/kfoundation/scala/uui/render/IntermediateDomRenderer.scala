// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui.render

import net.kfoundation.scala.UString.Interpolator
import net.kfoundation.scala.io.URL
import net.kfoundation.scala.parse.lex.CodeWalker
import net.kfoundation.scala.{UObject, UString}
import net.kfoundation.scala.util.Flow
import net.kfoundation.scala.util.Flow.Inlet
import net.kfoundation.scala.uui.MouseEvents.MouseData
import net.kfoundation.scala.uui._


object IntermediateDomRenderer {

  // --- NESTED CLASSED --- //

  private object Style {
    def apply(name: UString, value: UString): Style = new Style(name, value)
  }


  private class Style(val name: UString, val value: UString) extends UObject {
    def toStyles: Styles = new Styles(Seq(this))
    def add(item: Style): Styles = new Styles(Seq(this, item))
    def add(items: Iterable[Style]): Styles = new Styles(Seq(this) ++ items)
    override def appendTo(builder: UString.Builder): Unit =
      builder.appendAll(name, ':', value)
  }


  //noinspection ScalaUnusedSymbol
  private class Styles(val items: Seq[Style]) {
    def this(item: Style) = this(Seq(item))
    def this(items: Iterable[Style]) = this(items.toSeq)
    def add(item: Style) = new Styles(items.appended(item))
    def add(newItems: Iterable[Style]) = new Styles(items ++ newItems)
    def add(newItems: Styles) = new Styles(items ++ newItems.items)
    def remove(name: UString) = new Styles(items.filter(_.name != name))
    def isEmpty: Boolean = items.isEmpty
    def default(style: Style): Styles =
      if(items.exists(_.name.equals(style.name))) {
        this
      } else {
        add(style)
      }
    def default(styles: Styles): Styles =
      styles.items.foldLeft(this)((list, s) => list.default(s))
    def replace(style: Style): Styles = remove(style.name).add(style)
    def replace(styles: Styles): Styles =
      styles.items.foldLeft(this)((list, s) => list.replace(s))
    def styleAttrib = new Attrib("style",
      UString.join(items, U"; "))
  }


  class Attrib(val name: String, val value: String) extends UObject {
    def add(a: Attrib): Attribs = new Attribs(Seq(this, a))
    def add(a: Attribs): Attribs = new Attribs(a.items.prepended(this))
    def toAttribs: Attribs = new Attribs(Seq(this))
    override def appendTo(builder: UString.Builder): Unit =
      builder.appendAll(name, "=\"", value, '"')
  }


  object Attribs {
    val EMPTY: Attribs = new Attribs(Nil)
  }


  class Attribs(val items: Seq[Attrib]) {
    def add(a: Attrib): Attribs = new Attribs(items.appended(a))
    def add(a: Attribs): Attribs = new Attribs(items.appendedAll(a.items))
    def add(a: IterableOnce[Attrib]): Attribs = new Attribs(items.appendedAll(a))
  }


  class Event[T](val name: String, val handler: Inlet[T])


  trait Element {
    def trimEvents: Element
  }


  class StaticElement(val name: String, val attribs: Attribs,
    val events: Events) extends Element
  {
    override def trimEvents: Element =
      new StaticElement(name, attribs, events.trim)

    override def toString: String = s"<$name/>"
  }


  class ContainerElement(name: String, attribs:  Attribs,
    events: Events, val children: Seq[Element])
    extends StaticElement(name, attribs, events)
  {
    override def trimEvents: Element = new ContainerElement(
      name, attribs, events.trim, children.map(_.trimEvents))
  }


  class TextElement(name: String, attribs: Attribs,
    events: Events, val text: String)
    extends StaticElement(name, attribs, events)
  {
    override def trimEvents: Element = new TextElement(name, attribs,
      events.trim, text)
  }


  class EmbeddedElement(name: String, attribs: Attribs, events: Events,
    val html: UString)
    extends StaticElement(name, attribs, events)
  {
    override def trimEvents: Element = this
  }


  class DynamicElement(val incarnations: Flow[Element], val name: UString)
    extends Element
  {
    override def trimEvents: Element = this
    override def toString: String = "dynamic["+incarnations.peek+"]"
  }


  private object Arrangement {
    def apply(tag: String, add: Style, remove: String) =
      new Arrangement(tag, new Styles(add), remove)

    val H_FLOW: Arrangement = apply("span", Style("display", "inline-block"), "")
    val V_FLOW: Arrangement = apply("div", Style("display", "block"), "")
    val EMAIL_V_FLOW = new Arrangement("div", new Styles(Seq.empty), "")
    val RTL_ATTRIBS = new Attrib("dir", "rtl")
  }


  private class Arrangement(val tag: String, val styles: Styles,
    val minus: String, val isRtl:Boolean = false)
  {
    def styleAttrib = new Attrib("style",
      UString.join(styles.remove(minus).items, U"; "))

    def withStyle(fn: Styles => Styles): Arrangement =
      new Arrangement(tag, fn(styles), minus)

    def withRtl(rtl: Boolean): Arrangement =
      if(rtl == isRtl) this
      else new Arrangement(tag, styles, minus, rtl)

    def rtlAttrib: Option[Attrib] =
      if(isRtl) Some(Arrangement.RTL_ATTRIBS) else None
  }


  class Events {
    private var _mouseEvents: Seq[Event[MouseData]] = Nil
    private var _actionEvents: Seq[Event[Unit]] = Nil
    private var _textEvents: Seq[Event[UString]] = Nil

    private def toEvent[T](name: String, node: Inlet[T]): Event[T] =
      new Event[T](name, node)

    def addMouse(name: String, handler: Inlet[MouseData]): Events = {
      _mouseEvents = _mouseEvents.appended(toEvent(name, handler))
      this
    }

    def addAction(name: String, handler: Inlet[Unit]): Events = {
      _actionEvents = _actionEvents.appended(toEvent(name, handler))
      this
    }

    def addText(name: String, handler: Inlet[UString]): Events = {
      _textEvents = _textEvents.appended(toEvent(name, handler))
      this
    }

    def mouseEvents: Seq[Event[MouseData]] = _mouseEvents
    def actionEvents: Seq[Event[Unit]] = _actionEvents
    def textEvents: Seq[Event[UString]] = _textEvents

    def trim: Events = {
      val trimmed = new Events
      trimmed._textEvents = _textEvents.filter(_.handler.isActive)
      trimmed._actionEvents = _actionEvents.filter(_.handler.isActive)
      trimmed._mouseEvents = _mouseEvents.filter(_.handler.isActive)
      trimmed
    }
  }


  // --- FIELDS --- //

  private val V_FLOW_STYLE = new Styles(Seq(
    Style("display", "flex"),
    Style("flex-direction", "column")))

  private val SPACE = " "

  val DEFAULT = new IntermediateDomRenderer(false)


  // --- METHODS --- //

  def document(d: Document, traits: MediaTraits,
    state: Flow.Inlet[UString]): Element = DEFAULT.document(d, traits, state)
}



class IntermediateDomRenderer(forEmail: Boolean) {
  import IntermediateDomRenderer._
  import Length.LengthUnit._
  import TextStyle._
  import UString._

  // --- METHODS --- //

  // STYLE //

  private def length(l: Length): UString = String.format("%.0f", l.value) +
    (l.unit match {
      case PERCENT => "%"
      case PIXELS => "px"
      case POINTS => "pt"
      case CENTIMETERS => "cm"
      case MILLIMETERS => "mm"
      case INCHES => "in"
      case _ => throw new IllegalStateException()
    })


  private def lengthConstraint(name: String, l: LengthConstraint): Iterable[Style] =
    if(!l.isNone) {
      l.asExact
        .map(v => Seq(Style(name, length(v))))
        .getOrElse(l.min.map(v => Style(s"min-$name", length(v)))
          .concat(l.max.map(v => Style(s"max-$name", length(v)))))
    } else {
      Iterable.empty
    }


  private def gridLengthConstraint(l: LengthConstraint): UString =
    if(l.isNone)
      U"auto"
    else
      l.asExact
        .map(length)
        .getOrElse({
          val min = length(l.min.getOrElse(Length.of(0).px))
          val max = length(l.max.getOrElse(Length.of(0).px))
          U"minmax($min, $max)"
        })


  private def sizeConstraint(s: SizeConstraint): Iterable[Style] =
    lengthConstraint("width", s.width) ++ lengthConstraint("height", s.height)


  private def cardinalLength(name: String, s: Cardinal[Length]): Option[Style] =
    Some(Style(name, s.asExact
      .map(length)
      .getOrElse(s.toSeq
        .map(length)
        .mkString(" "))))


  private def color(c: Color, default: Option[Color] = None): Option[String] =
    if(default.contains(c))
      None
    else {
      val rgba = c.asRgba
      val t = rgba.encode32bit
      if(rgba.alpha == 1) {
        Some(s"rgb(${t._1}, ${t._2}, ${t._3})")
      } else {
        Some(s"rgba(${t._1}, ${t._2}, ${t._3}, ${rgba.alpha})")
      }
    }


  private def backgroundColor(c: Color): Option[Style] =
    color(c, Some(Color.CLEAR)).map(Style("background-color", _))


  private def angle(a: Angle): String = a.toDegrees.amount.toInt.toString + "deg"


  private def linearGradient(g: Fill.LinearGradient): String = "linear-gradient(" +
    angle(g.angle) + ", " + color(g.color1) + ", " + color(g.color2) + ")"


  private def position(p: Position): Option[Style] =
    if(p.equals(Position.ZERO))
      None
    else
      Some(Style("background-position", length(p.left) + " " + length(p.top)))


  private def scaleLength(l: Option[Length]): UString =
    l.map(length).getOrElse(U"auto")


  private def scale(s: Scale): Option[Style] =
    if(s.equals(Scale.NONE))
      None
    else
      Some(Style(
        "background-size",
        scaleLength(s.width) + " " + scaleLength(s.height)))


  private def repeat(repeat: Fill.Repetition): Option[String] = repeat match {
    case Fill.REPEAT_X => Some("repeat-x")
    case Fill.REPEAT_Y => Some("repeat-y")
    case Fill.REPEAT_XY => None
    case Fill.NO_REPEAT => Some("no-repeat")
    case _ => throw new IllegalStateException()
  }


  private def image(i: Fill.Image): Styles =
     Style("background-image", s"url(${i.url})")
       .add(position(i.position))
       .add(scale(i.scale))
       .add(repeat(i.repeat).map(Style("background-repeat", _)))


  private def fill(f: Fill): Styles = f match {
    case c: Fill.ColorFill => new Styles(backgroundColor(c.color))
    case g: Fill.LinearGradient => Style("background-image", linearGradient(g)).toStyles
    case i: Fill.Image => image(i)
  }


  private def linePattern(p: LinePattern): String = {
    import LinePattern._
    p match {
      case SOLID => "solid"
      case DASHED => "dashed"
      case DOTTED => "dotted"
      case DOUBLE => "double"
      case _ => throw new IllegalStateException()
    }
  }


  private def border(name: String, opt: Option[BorderStyle]): Option[Style] =
    opt.map(b => Style(name, length(b.size) + " " + linePattern(b.pattern) +
      " " + color(b.color).getOrElse(Style("color", "black"))))


  private def borders(b: OptionCardinal[BorderStyle]): Iterable[Style] =
    if(b.isUniform) {
      border("border", b.left)
    } else {
      border("border-top", b.top) ++ border("border-right", b.right) ++
        border("border-bottom", b.bottom) ++ border("border-left", b.left)
    }


  private def corners(c: Cardinal[Int]): Option[Style] =
    if(c.isUniform) {
      if(c.top != 0) {
        Some(Style("border-radius", c.top.toString + "px"))
      } else {
        None
      }
    } else {
      Some(Style("border-radius", c.toSeq.map(_ + "ps").mkString(" ")))
    }


  private def shadow(opt: Option[Shadow]): Option[Style] = opt.map(s =>
    Style("box-shadow", s.x.toString + "px " + s.y + "px " + s.blurRadius + "px " +
      color(s.color).getOrElse("")))


  private def blur(radius: Double): Option[Style] =
    if(radius == 0) {
      None
    } else {
      Some(Style("backdrop-filter", s"blur(${radius}px)"))
    }


  private def rectStyle(s: RectStyle): Styles = new Styles(
    sizeConstraint(s.size) ++
    cardinalLength("padding", s.padding) ++
    cardinalLength("margin", s.margin) ++
    borders(s.border) ++
    corners(s.cornerRadii) ++
    shadow(s.shadow) ++
    blur(s.blur) ++
    pointer(s.cursor))
    .add(fill(s.fill))


  private def fontFamily(opt: Option[UString]): Option[Style] =
    opt.map(f => Style("font-family",s"'$f'"))


  private def fontSize(opt: Option[Int]): Option[Style] =
    opt.map(i => Style("font-size", s"${i}pt"))


  private def fontWeight(w: FontWeight): Option[Style] =
    if(w == FontWeight.NORMAL) None else Some(Style("font-weight", "bold"))


  private def fontStyle(s: FontStyle): Option[Style] =
    if(s == FontStyle.NORMAL) None else Some(Style("font-style", "italic"))


  private def textDecoration(d: TextDecoration): Option[Style] =
    if(d == TextDecoration.NONE) None else Some(Style("text-decoration", "underline"))


  private def textStyle(s: TextStyle): Styles = new Styles(
    fontFamily(s.family) ++
    fontSize(s.size) ++
    color(s.color, Some(Color.BLACK)).map(Style("color", _)) ++
    fontWeight(s.weight) ++
    fontStyle(s.style) ++
    textDecoration(s.decoration))


  private def hAlign(a: HAlign): Option[String] = a match {
    case HAlign.LEFT => None
    case HAlign.CENTER => Some("center")
    case HAlign.RIGHT => Some("right")
    case _ => throw new IllegalStateException()
  }


  private def textAlign(a: HAlign): Option[Style] =
    hAlign(a).map(Style("text-align", _))


  private def vFlowAlign(a: HAlign): Option[Style] = (a match {
    case HAlign.LEFT => None
    case HAlign.CENTER => Some("center")
    case HAlign.RIGHT => Some("flex-end")
    case _ => throw new IllegalStateException()
  }).map(Style("align-items", _))


  private def hFlowAlign(a: HAlign): Option[Style] = (a match {
    case HAlign.LEFT => None
    case HAlign.CENTER => Some("center")
    case HAlign.RIGHT => Some("flex-end")
    case _ => throw new IllegalStateException()
  }).map(Style("justify-content", _))


  private def pointer(s: Cursor): Option[Style] = (s match {
    case Cursor.ARROW => None
    case Cursor.HAND => Some("pointer")
    case Cursor.CROSSHAIR => Some("crosshair")
    case Cursor.FORBIDDEN => Some("not-allowed")
    case Cursor.HELP => Some("help")
    case Cursor.WAIT => Some("wait")
    case _ => throw new IllegalStateException()
  }).map(Style("cursor", _))


  private def gridStyle(cells: Seq[Content.BandCell], isVertical: Boolean): Seq[Style] =
    Seq(
      Style("display", "grid"),
      Style(
        if(isVertical) "grid-template-rows" else "grid-template-columns",
        cells.map(c => gridLengthConstraint(c.size))
          .mkString(" ")))


  private def gridColumns(cols: LengthConstraintList): Style =
    new Style("grid-template-columns", cols.items.map(gridLengthConstraint).mkString(" "))


  private def gridRowHeight(rowSize: LengthConstraint): Style =
    new Style("grid-template-rows", gridLengthConstraint(rowSize))


  private def gridGap(name: String, value: Length): Style =
    new Style(name, length(value))


  private def gridCellVAlign(align: VAlign): Option[Style] = (align match {
    case VAlign.TOP => None
    case VAlign.MIDDLE => Some("center")
    case VAlign.BOTTOM => Some("end")
    case _ => throw new IllegalStateException()
  }).map(new Style("align-self", _))


  private def listStyle(s: ListStyle): Style = {
    import ListStyle._
    val value = s match {
      case DISK => "disk"
      case CIRCLE => "circle"
      case SQUARE => "square"
      case ARMENIAN => "armenian"
      case DECIMAL => "decimal"
      case DECIMAL_LEADING_ZERO => "decimal-leading-zero"
      case GEORGIAN => "georgian"
      case HEBREW => "hebrew"
      case HIRAGANA => "hiragana"
      case HIRAGANA_IROHA => "hiragana-iroha"
      case KATAKANA => "katakana"
      case KATAKANA_IROHA => "katakana-iroha"
      case LOWER_ALPHA => "lower-alpha"
      case LOWER_GREEK => "lower-greek"
      case LOWER_LATIN => "lower-latin"
      case LOWER_ROMAN => "lower-romain"
      case UPPER_ALPHA => "upper-alpha"
      case UPPER_LATIN => "upper-latin"
      case UPPER_ROMAN => "upper-roman"
      case _ => throw new IllegalArgumentException
    }
    Style("list-style-type", value)
  }

  private def wordWrap(w: WordWrap): Option[Style] = w match {
    case WordWrap.WORD_BREAK => None
    case WordWrap.ANYWHERE => Some(Style("overflow-wrap", "anywhere"))
    case _ => throw new IllegalArgumentException
  }

  private def href(url: URL): Attrib = new Attrib("href", url.toUString)


  // ATTRIB ///

  private def nameAttrib(c: Content): Attrib = new Attrib("id", c.name)


  private def rectEvents(c: Content.Rect): Events =
    new Events().addMouse("click", c.events.onClick)
      .addMouse("mouseenter", c.events.onEnter)
      .addMouse("mouseleave", c.events.onLeave)
      .addMouse("mousemove", c.events.moves)


  private def placeholderAttrib(t: Content.TextInput): Option[Attrib] = t.placeholder
    .map(p => new Attrib("placeholder", p))


  private def typeAttrib(t: Content.TextInput): Attrib =
    if(t.isMasked) new Attrib("type", "password")
    else new Attrib("type", "text")


  private def valueAttrib(s: UString): Attrib = new Attrib("value", s)


  // CONTENT //

  private def empty(c: Content.Empty) = new StaticElement("span",
    new Attrib("style", "display: none").add(nameAttrib(c)),
    new Events)


  private def rect(ar: Arrangement, c: Content.Rect): TextElement =
    new TextElement(ar.tag, ar.styleAttrib.add(nameAttrib(c)),
      rectEvents(c), SPACE)


  private def text(ar: Arrangement, t: Content.Text): TextElement =
    new TextElement(
      ar.tag,
      ar.withStyle(_.add(textStyle(t.textStyle))
          .add(wordWrap(t.wordWrap))
          .add(textAlign(t.align)))
        .styleAttrib
        .add(nameAttrib(t))
        .add(ar.rtlAttrib),
      rectEvents(t),
      t.value)


  private def link(ar: Arrangement, t: Content.Link): TextElement =
    new TextElement(
      "a",
      ar.withStyle(_.add(textStyle(t.textStyle)))
        .styleAttrib
        .add(href(t.href))
        .add(new Attrib("target", "_blank"))
        .add(nameAttrib(t))
        .add(ar.rtlAttrib),
      rectEvents(t),
      t.text)


  private def image(ar: Arrangement, i: Content.Image): StaticElement =
    new StaticElement("img",
      attribs = new Attrib("src", i.url)
        .add(ar.styleAttrib)
        .add(nameAttrib(i)),
      events = rectEvents(i))


  private def textInput(ar: Arrangement, t: Content.TextInput): StaticElement =
    new StaticElement("input",
      Style("border", "none")
        .add(Style("background-color", "#00000000"))
        .replace(ar.styles)
        .add(textStyle(t.textStyle))
        .styleAttrib
        .add(typeAttrib(t))
        .add(valueAttrib(t.value))
        .add(nameAttrib(t))
        .add(placeholderAttrib(t))
        .add(ar.rtlAttrib),
      rectEvents(t)
        .addText("keypress", t.valueChanges))


  private def button(ar: Arrangement, b: Content.Button): StaticElement =
    new TextElement("button",
      ar.withStyle(_.add(textStyle(b.textStyle)))
        .styleAttrib
        .add(nameAttrib(b)),
      rectEvents(b)
        .addAction("click", b.pressEvents),
      b.caption)


  private def vBand(ar: Arrangement, b: Content.VerticalBand): ContainerElement =
    new ContainerElement(
      name = ar.tag,
      attribs = ar.withStyle(_
          .add(gridStyle(b.cells, true))
          .add(gridGap("row-gap", b.gap)))
        .styleAttrib
        .add(nameAttrib(b)),
      events = rectEvents(b),
      children = b.cells
        .zipWithIndex
        .map(t => content(
          new Arrangement(
            "div",
            Style("grid-row", (t._2+1).toString).add(gridCellVAlign(t._1.vAlign)),
            "height", ar.isRtl),
          t._1.content)))


  private def hBand(ar: Arrangement, b: Content.HorizontalBand): StaticElement =
    new ContainerElement(
      name = ar.tag,
      attribs = ar.withStyle(_
          .add(gridStyle(b.cells, false))
          .add(gridGap("column-gap", b.gap)))
        .styleAttrib
        .add(nameAttrib(b)),
      events = rectEvents(b),
      children = b.cells
        .zipWithIndex
        .map(t => content(
          new Arrangement(
            "span",
            Style("grid-column", (t._2+1).toString)
              .add(gridCellVAlign(t._1.vAlign)),
            "", ar.isRtl),
          t._1.content)))


  private def hFlow(ar: Arrangement, b: Content.HorizontalFlow): StaticElement =
    new ContainerElement(
      name = ar.tag,
      attribs = ar.withStyle(_
          .add(hFlowAlign(b.align))
          .replace(Style("display", "flex")))
        .styleAttrib
        .add(nameAttrib(b)),
      events = rectEvents(b),
      children = b.contents.map(content(Arrangement.H_FLOW.withRtl(ar.isRtl), _)))


  private def vFlow(ar: Arrangement, b: Content.VerticalFlow): StaticElement =
    if(forEmail) {
      new ContainerElement(
        name = ar.tag,
        attribs = ar.withStyle(_
          .add(textAlign(b.hAlign)))
          .styleAttrib
          .add(nameAttrib(b)),
        events = rectEvents(b),
        children = b.contents.map(content(Arrangement.EMAIL_V_FLOW.withRtl(ar.isRtl), _)))
    } else {
      new ContainerElement(
        name = ar.tag,
        attribs = ar.withStyle(_
          .add(V_FLOW_STYLE)
          .add(vFlowAlign(b.hAlign)))
          .styleAttrib
          .add(nameAttrib(b)),
        events = rectEvents(b),
        children = b.contents.map(content(Arrangement.V_FLOW.withRtl(ar.isRtl), _)))
    }


  private def free(ar: Arrangement, b: Content.Free): StaticElement =
    new ContainerElement(
      ar.tag,
      ar.styleAttrib.add(nameAttrib(b)),
      rectEvents(b),
      b.items.zip(b.items.indices.reverse)
        .map(c => content(
        new Arrangement("div",
          Style("position", "absolute")
            .add(Style("left", length(c._1.x)))
            .add(Style("top", length(c._1.y)))
            .add(Style("z-index", c._2.toString)),
          "", ar.isRtl),
        c._1.item)))


  private def composed(arrange: Arrangement, b: Content.Composite): Element =
    content(arrange, b.apply())


  private def dynamic(arrange: Arrangement, d: Content.Dynamic): DynamicElement =
    new DynamicElement(d.apply().map(content(arrange, _)), d.name)


  private def styled(ar: Arrangement, s: Content.Styled): Element = content(
      new Arrangement(
        ar.tag,
        ar.styles.default(rectStyle(s.style)),
        ar.minus, ar.isRtl),
      s.content)


  private def grid(ar: Arrangement, g: Content.Grid): Element = {
    val innerAr = new Arrangement("div", new Styles(Nil), "", ar.isRtl)
    new ContainerElement(
      ar.tag,
      ar.withStyle(
        _.replace(Style("display", "grid"))
          .add(gridColumns(g.columns))
          .add(gridRowHeight(g.rowHeight))
          .add(gridGap("column-gap", g.columnGap))
          .add(gridGap("row-gap", g.rowGap)))
        .styleAttrib
        .add(nameAttrib(g)),
      rectEvents(g),
      g.contents.map(content(innerAr, _)))
  }


  private def list(ar: Arrangement, style: ParagraphStyle, l: Content.List):
    Element =
  {
    val tag = l.style match {
      case _: ListStyle.Bulleted => "ul"
      case _ => "ol"
    }

    val lStyle = listStyle(l.style)
    val childrenArrangement = new Arrangement("li", new Styles(Nil), "")
    val children = l.items.map(c => typesetItem(childrenArrangement, style, c))

    new ContainerElement(
      tag,
      ar.withStyle(_.add(lStyle))
        .styleAttrib
        .toAttribs,
      new Events,
      children)
  }


  private def lotex(dom: LoTexDocumentModel, styles: ParagraphStyle):
    Seq[TextElement] =
  {
    dom.parts.map(p => {
      val styleAttrib = textStyle(styles.styleMap
        .getOrElse(p.formatName, styles.base))
        .styleAttrib
      p match {
        case l: LoTexDocumentModel.LinkedText => new TextElement(
          "a", styleAttrib.add(new Attrib("href", l.href.toString()))
            .add(new Attrib("target", "_blank")), new Events, l.text)
        case t: LoTexDocumentModel.FormattedText => new TextElement(
          "span", styleAttrib.toAttribs, new Events, t.text)
      }
    })
  }


  private def paragraph(ar: Arrangement, style: ParagraphStyle,
    p: Content.Paragraph): Element =
  {
    val children = lotex(LoTexDocumentModel.PARSER
      .tryRead(CodeWalker.of(p.text)).get, style)
    new ContainerElement(ar.tag,
      ar.withStyle(_
          .default(Style("margin-top", "1em"))
          .default(Style("margin-bottom", "1em")))
        .styleAttrib.toAttribs,
      new Events, children)
  }


  private def typesetItem(ar: Arrangement, style: ParagraphStyle,
      i: Content.TypesetItem): Element = i match
  {
    case l: Content.List => list(ar, style, l)
    case p: Content.Paragraph => paragraph(ar, style, p)
  }


  private def typeset(ar: Arrangement, t: Content.Typeset): ContainerElement = {
    val childrenArrangement = new Arrangement("div", new Styles(Nil), "")
    new ContainerElement(
      ar.tag,
      ar.withStyle(_.add(textStyle(t.style.base)))
        .styleAttrib
        .add(nameAttrib(t))
        .add(ar.rtlAttrib),
      rectEvents(t),
      t.content.map(c => typesetItem(childrenArrangement, t.style, c)))
  }


  private def embedded(a: Arrangement, e: Content.EmbedHTML): EmbeddedElement =
    new EmbeddedElement(a.tag, a.styleAttrib.add(nameAttrib(e)),
      new Events, e.rawHtml)


  private def rtl(a: Arrangement, r: Content.WritingDirection): Element =
    content(a.withRtl(r.isRtl), r.content)


  private def content(a: Arrangement, c: Content): Element = c match {
    case t: Content.Text => text(a, t)
    case l: Content.Link => link(a, l)
    case i: Content.Image => image(a, i)
    case t: Content.TextInput => textInput(a, t)
    case b: Content.Button => button(a, b)
    case t: Content.Typeset => typeset(a, t)
    case e: Content.EmbedHTML => embedded(a, e)
    case g: Content.Grid => grid(a, g)
    case b: Content.HorizontalBand => hBand(a, b)
    case b: Content.VerticalBand => vBand(a, b)
    case f: Content.VerticalFlow => vFlow(a, f)
    case f: Content.HorizontalFlow => hFlow(a, f)
    case f: Content.Free => free(a, f)
    case cc: Content.Composite => composed(a, cc)
    case d: Content.Dynamic => dynamic(a, d)
    case r: Content.Rect => rect(a, r)
    case r: Content.WritingDirection => rtl(a, r)
    case s: Content.Styled => styled(a, s)
    case e: Content.Empty => empty(e)
    case _ => throw new RuntimeException(c.toString)
  }


  def document(d: Document, traits: MediaTraits,
      state: Flow.Inlet[UString]): Element =
    new DynamicElement(
      d.apply(traits, state)
        .map(content(Arrangement("div", Style("width", "100%"), ""), _)),
      "document")
}