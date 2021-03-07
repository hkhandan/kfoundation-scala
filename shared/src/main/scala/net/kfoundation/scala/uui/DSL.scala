// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui

import net.kfoundation.scala.UString
import net.kfoundation.scala.io.URL
import net.kfoundation.scala.util.{Flow, Zero}
import net.kfoundation.scala.uui.Fill.ColorFill

import scala.language.implicitConversions

/** The Content DSL */
object DSL {
  import Content._

  // Angle

  implicit def angleScalar(amount: Double): Angle.Scalar = Angle.of(amount)
  implicit val ZERO_ANGLE: Zero[Angle] = new Zero[Angle](Angle.ZERO)
  implicit def of(t: (Length, LinePattern, Color)): BorderStyle =
    new BorderStyle(t._1, t._2, t._3)


  // Border

  val NO_BORDERS: OptionCardinal[BorderStyle] = BorderStyle.NONE

  def top(size: Length, style: LinePattern, color: Color):
    OptionCardinal[BorderStyle] =
      OptionCardinal.top(of(size, style, color))

  def bottom(size: Length, style: LinePattern, color: Color):
    OptionCardinal[BorderStyle] =
      OptionCardinal.bottom(of(size, style, color))

  def left(size: Length, style: LinePattern, color: Color):
    OptionCardinal[BorderStyle] =
      OptionCardinal.left(of(size, style, color))

  def right(size: Length, style: LinePattern, color: Color):
    OptionCardinal[BorderStyle] =
      OptionCardinal.right(of(size, style, color))

  def all(size: Length, style: LinePattern, color: Color):
    OptionCardinal[BorderStyle] =
      OptionCardinal(of(size, style, color))

  implicit def all(t: (Length, LinePattern, Color)):
    OptionCardinal[BorderStyle] = all(t._1, t._2, t._3)


  // Color

  val BLACK: Color = Color.BLACK
  val WHITE: Color = Color.WHITE
  val CLEAR: Color = Color.CLEAR

  def rgb(red: Double, green: Double, blue: Double): Color.RgbColor =
    Color.rgb(red, green, blue)

  def rgb24b(red: Int, green: Int, blue: Int): Color.RgbColor =
    Color.rgb24b(red, green, blue)

  def rgba(red: Double, green: Double, blue: Double, alpha: Double):
    Color.RgbaColor = Color.rgba(red, green, blue, alpha)

  def rgba32b(red: Byte, green: Byte, blue: Byte, alpha: Byte):
    Color.RgbaColor = Color.rgba(red, green, blue, alpha)

  def cmyk(cyan: Double, magenta: Double, yellow: Double, black: Double):
    Color.CmykColor = Color.cmyk(cyan, magenta, yellow, black)

  def gray(value: Double): Color.GrayScaleColor = Color.gray(value)

  def gray8b(value: Int): Color.GrayScaleColor = Color.gray8b(value)


  // Fill

  implicit def colorFill(color: Color): ColorFill = new ColorFill(color)


  // Int

  implicit val ZERO_INT: Zero[Int] = new Zero(0)
  implicit def toCardinalInt(option: OptionCardinal[Int]): Cardinal[Int] =
    Cardinal.of(option)
  def top(c: Int): OptionCardinal[Int] = OptionCardinal.top(c)
  def right(c: Int): OptionCardinal[Int] = OptionCardinal.right(c)
  def bottom(c: Int): OptionCardinal[Int] = OptionCardinal.bottom(c)
  def left(c: Int): OptionCardinal[Int] = OptionCardinal.left(c)
  def all(c: Int): OptionCardinal[Int] = OptionCardinal(c)


  // Length

  implicit val ZERO_LENGTH: Zero[Length] = new Zero(Length.ZERO)

  implicit def lengthScalar(value: Double): Length.Scalar = Length.of(value)
  implicit def lengthScalar(value: Int): Length.Scalar = Length.of(value)

  implicit def toCardinal(option: OptionCardinal[Length]): Cardinal[Length] =
    Cardinal.of(option)

  implicit def toCardinal(t: (Length, Length, Length, Length)):
    Cardinal[Length] = Cardinal(t._1, t._2, t._3, t._4)

  def top(c: Length): OptionCardinal[Length] = OptionCardinal.top(c)
  def right(c: Length): OptionCardinal[Length] = OptionCardinal.right(c)
  def bottom(c: Length): OptionCardinal[Length] = OptionCardinal.bottom(c)
  def left(c: Length): OptionCardinal[Length] = OptionCardinal.left(c)
  def all(c: Length): OptionCardinal[Length] = OptionCardinal(c)


  // Length Constraint

  val FREE_LENGTH: LengthConstraint = LengthConstraint.NONE

  implicit def exactly(m: Length): LengthConstraint =
    new LengthConstraint(Some(m), Some(m))

  def min(m: Length): LengthConstraint = new LengthConstraint(Some(m), None)

  def max(m: Length): LengthConstraint = new LengthConstraint(None, Some(m))

  def between(min: Length, max: Length): LengthConstraint =
    new LengthConstraint(Some(min), Some(max))

  def top(c: LengthConstraint): OptionCardinal[LengthConstraint] =
    OptionCardinal.top(c)

  def right(c: LengthConstraint): OptionCardinal[LengthConstraint] =
    OptionCardinal.right(c)

  def bottom(c: LengthConstraint): OptionCardinal[LengthConstraint] =
    OptionCardinal.bottom(c)

  def left(c: LengthConstraint): OptionCardinal[LengthConstraint] =
    OptionCardinal.left(c)

  def all(c: LengthConstraint): OptionCardinal[LengthConstraint] = OptionCardinal(c)

  def cardinal(top: LengthConstraint, left: LengthConstraint,
      bottom: LengthConstraint, right: LengthConstraint):
    OptionCardinal[LengthConstraint] =
      OptionCardinal(top, left, bottom, right)

  implicit def cardinal(t: (LengthConstraint, LengthConstraint,
    LengthConstraint, LengthConstraint)): OptionCardinal[LengthConstraint] =
      OptionCardinal(t._1, t._2, t._3, t._4)


  // Scale

  val FREE_SCALE: Scale = Scale.NONE

  def scaleWidth(w: Length): Scale = Scale.width(w)
  def scaleHeight(h: Length): Scale = Scale.height(h)
  def scale(width: Length, height: Length): Scale = Scale(width, height)


  // SizeConstraint

  val FREE_SIZE: SizeConstraint = SizeConstraint.NONE

  implicit def exactly(t: (Length, Length)): SizeConstraint =
    new SizeConstraint(exactly(t._1), exactly(t._2))

  implicit def size(t: (LengthConstraint, LengthConstraint)): SizeConstraint =
    new SizeConstraint(t._1, t._2)

  implicit def square(widthAndHeight: LengthConstraint):
    SizeConstraint = new SizeConstraint(widthAndHeight, widthAndHeight)

  def width(w: LengthConstraint): SizeConstraint =
    new SizeConstraint(w, LengthConstraint.NONE)

  def height(h: LengthConstraint): SizeConstraint =
    new SizeConstraint(LengthConstraint.NONE, h)

  def size(w: LengthConstraint, h: LengthConstraint): SizeConstraint =
    new SizeConstraint(w, h)


  // Shadow

  def shadow(x: Int = 0, y: Int = 0, blur: Int = 0,
      color: Color = Shadow.DEFAULT_COLOR): Option[Shadow] =
    Some(new Shadow(x, y, blur, color))


  // Content

  implicit def toSeqBuilder(c: Content): SeqBuilder[Content] = new SeqBuilder(c)
  implicit def toSeqBuilder(c: BandCell): SeqBuilder[BandCell] = new SeqBuilder(c)
  implicit def toSeqBuilder(c: FreePlacement): SeqBuilder[FreePlacement] = new SeqBuilder(c)
  implicit def toSeqBuilder(c: TypesetItem): SeqBuilder[TypesetItem] = new SeqBuilder(c)
  implicit def toSeq(c: Content): Seq[Content] = Seq(c)
  implicit def toSeq[T](c: SeqBuilder[T]): Seq[T] = c.toSeq

  def empty(name: UString = autoName("empty")) = new Empty(name)

  def rect(
      events: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("rect")) =
    new Rect(events, name)

  def text(
      value: UString,
      font: TextStyle = TextStyle.DEFAULT,
      align: HAlign = HAlign.LEFT,
      wordWrap: WordWrap = WordWrap.WORD_BREAK,
      events: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("text")) =
    new Text(value, font, align, wordWrap, events, name)

  def link(text: UString, href: URL,
      font: TextStyle = TextStyle.DEFAULT,
      events: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("link")) =
    new Link(text, href, font, events, name)

  def image(url: UString,
      events: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("image")) =
    new Image(url, events, name)

  def textInput(
      value: UString,
      isMasked: Boolean = false,
      placeholder: Option[UString] = None,
      textStyle: TextStyle = TextStyle.DEFAULT,
      placeholderColor: Color = TextInput.DEFAULT_PLACEHOLDER_COLOR,
      semantics: TextInput.Semantics = TextInput.Semantics.NONE,
      valueChanges: Flow.Inlet[UString] = Flow.closed,
      mouseEvents: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("textInput")) =
    new TextInput(value, isMasked, placeholder, textStyle, placeholderColor,
      semantics, valueChanges, mouseEvents, name)

  def button(
      caption: UString,
      textStyle: TextStyle = TextStyle.DEFAULT,
      semantics: Button.Semantics = Button.Semantics.NONE,
      pressEvents: Flow.Inlet[Unit] = Flow.closed,
      events: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("button")) =
    new Button(caption, textStyle, semantics, pressEvents, events, name)

  def checkbox(
      caption: Text,
      checked: Boolean = false,
      events: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("checkBox")) =
    new CheckBox(checked, caption, events, name)

  def vBand(cells: Seq[BandCell],
      gap: Length = Length.ZERO,
      semantics: Container.Semantics = Container.Semantics.NONE,
      events: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("vBand")) =
    new VerticalBand(cells, gap, semantics, events, name)

  def hBand(cells: Seq[BandCell],
      gap: Length = Length.ZERO,
      semantics: Container.Semantics = Container.Semantics.NONE,
      events: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("hBand")) =
    new HorizontalBand(cells, gap, semantics, events, name)

  def vFlow(contents: Seq[Content],
      hAlign: HAlign = HAlign.LEFT,
      semantics: Container.Semantics = Container.Semantics.NONE,
      events: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("vFlow")) =
    new VerticalFlow(contents, hAlign, semantics, events, name)

  def hFlow(contents: Seq[Content],
      align: HAlign = HAlign.LEFT,
      semantics: Container.Semantics = Container.Semantics.NONE,
      events: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("hFlow")) =
    new HorizontalFlow(contents, align, semantics, events, name)

  def free(items: Seq[FreePlacement],
      semantics: Container.Semantics = Container.Semantics.NONE,
      events: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("free")) =
    new Free(items, semantics, events, name)

  def grid(contents: Seq[Content], columns: LengthConstraintList,
      rowHeight: LengthConstraint = FREE_LENGTH,
      columnGap: Length = Length.ZERO,
      rowGap: Length = Length.ZERO,
      semantics: Container.Semantics = Container.Semantics.NONE,
      events: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("grid")) =
    new Grid(contents, columns, rowHeight, columnGap, rowGap, semantics,
      events, name)

  def row(cells: Seq[Content],
      height: LengthConstraint = LengthConstraint.NONE) =
    new TableRow(cells, height)

  def table(
      cols: Seq[LengthConstraint],
      rows: Seq[TableRow],
      semantics: Container.Semantics = Container.Semantics.NONE,
      events: MouseEvents = MouseEvents.NONE,
      name: UString = autoName("table")) =
    new Table(cols, rows, semantics, events, name)

  def dynamic(f: Flow[_ <: Content],
    name: UString = autoName("dynamic")): Dynamic =
    new Dynamic(name) {
      override def apply(): Flow[_ <: Content] = f
    }

  def typeset(
      paragraphs: Seq[TypesetItem],
      style: ParagraphStyle = ParagraphStyle.DEFAULT,
      events: MouseEvents = NO_MOUSE_EVENTS,
      name: UString = autoName("typeset")): Typeset =
    new Typeset(paragraphs, style, events, name)

  def typeset(
      text: UString,
      style: ParagraphStyle): Typeset =
    typeset(Seq(paragraph(text)), style)

  def paragraph(text: UString): Paragraph = new Paragraph(text)

  def list(
    items: Seq[TypesetItem],
    style: ListStyle = ListStyle.DISK): List =
    new List(items, style)

  def embeddedHtml(
      html: UString,
      name: UString = autoName("embedded")): EmbedHTML =
    new EmbedHTML(html, name)


  // Events

  val NO_MOUSE_EVENTS: MouseEvents = MouseEvents.NONE

  def mouseEvents(
      enter: Flow.Inlet[MouseEvents.MouseData] = Flow.inlet,
      leave: Flow.Inlet[MouseEvents.MouseData] = Flow.inlet,
      move: Flow.Inlet[MouseEvents.MouseData] = Flow.inlet,
      click: Flow.Inlet[MouseEvents.MouseData] = Flow.inlet) =
    new MouseEvents(enter, leave, move, click)


  // Auto

  object AUTO
  implicit def toLengthConstraint(a: AUTO.type): LengthConstraint = FREE_LENGTH
  implicit def toSizeConstraint(a: AUTO.type): SizeConstraint = FREE_SIZE
  implicit def toScale(a: AUTO.type ): Scale = FREE_SCALE
}
