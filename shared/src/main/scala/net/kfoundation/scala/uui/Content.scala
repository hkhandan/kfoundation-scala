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
import net.kfoundation.scala.util.Flow

import java.util.concurrent.atomic.AtomicInteger


/** A content that can be rendered on an output media. */
class Content(val name: UString) {
  import Content._

  /**
   * Creates a BandCell with no length constraint, containing this Content
   * object.
   */
  implicit def inCell: BandCell = new BandCell(this, LengthConstraint.NONE,
    VAlign.TOP)


  /**
   * Creates a BandCell with the given length constraint and alignment,
   * containing this Content object.
   */
  def inCell(length: LengthConstraint, vAlign: VAlign = VAlign.TOP) =
    new BandCell(this, length, vAlign)


  /**
   * Bundles this Content object with an XY coordinate.
   */
  def at(x: Length, y: Length) = new FreePlacement(x, y, this)


  /**
   * Bundles this Content object with the XY coordinate of the origin (0, 0).
   */
  def atOrigin: FreePlacement =
    new FreePlacement(Length.of(0).px, Length.of(0).px, this)


  /**
   * Bundles this Content object with a RectStyle specifier.
   */
  def withStyle(s: RectStyle): Styled = new Styled(this, s)


  /**
   * Bundles this Content object with a RectStyle specifier as described by
   * the given parameters.
   */
  def withStyle(
      size: SizeConstraint = SizeConstraint.NONE,
      margin: Cardinal[Length] = RectStyle.ALL_ZERO_LENGTH,
      padding: Cardinal[Length] = RectStyle.ALL_ZERO_LENGTH,
      border: OptionCardinal[BorderStyle] = BorderStyle.NONE,
      cornersRadii: Cardinal[Int] = RectStyle.ALL_ZERO_INT,
      shadow: Option[Shadow] = None,
      blur: Double = 0,
      fill: Fill = Fill.color(Color.CLEAR),
      cursor: Cursor = Cursor.ARROW): Styled =
    withStyle(RectStyle(size, margin, padding, border, cornersRadii, shadow,
      blur, fill, cursor))


  /**
   * Bundles this Content object with instruction for writting direction.
   */
  def withWritingDirection(isRtl: Boolean): Content.WritingDirection =
    new WritingDirection(isRtl, this)


  override def toString: String = s"Content($name)"
}



object Content {
  /**
   * A Composite content allows its content to be composed using apply()
   * function right before being rendered.
   */
  abstract class Composite extends Content(autoName("composite"))
    with (() => Content)


  /**
   * Use a Dynamic content when the appearance of it is to be changed over time.
   */
  abstract class Dynamic(name: UString = autoName("dynamic"))
    extends Content(name)
  {
    def apply(): Flow[_ <: Content]
  }


  /**
   * A Content bundled with instruction about writting direction.
   */
  class WritingDirection(val isRtl: Boolean, val content: Content)
    extends Content("RTL")
  {
    override def withWritingDirection(_isRtl: Boolean): WritingDirection =
      if(_isRtl == isRtl) this
      else new WritingDirection(_isRtl, content)
  }


  /**
   * A Content bundled with style specifications.
   */
  class Styled(val content: Content, val style: RectStyle)
    extends Content(autoName("styled"))
  {
    def withStyle(fn: RectStyle => RectStyle): Styled =
      new Styled(content, fn(style))
    override def withStyle(s: RectStyle): Styled =
      new Styled(content, s)
    override def toString: String = s"Styled($content)"
  }


  /**
   * An empty i.e. no content. This is specially useful to hide a dynamic
   * content.
   */
  class Empty(name: UString) extends Content(name)


  /**
   * A plain rectangular region.
   */
  class Rect(val events: MouseEvents, name: UString) extends Content(name)


  object Container {
    object Semantics extends Enumeration {
      val NONE, INPUT, LOGIN, RADIO_GROUP = Value
    }
    type Semantics = Semantics.Value
  }


  /** A content that is meant to contain other contents. */
  class Container(
      val contents: Seq[Content],
      val semantics: Container.Semantics,
      events: MouseEvents,
      name: UString)
    extends Rect(events, name)


  /** A formatted text content */
  class Text(val value: UString, val textStyle: TextStyle, val align: HAlign,
      val wordWrap: WordWrap, events: MouseEvents, name: UString)
    extends Rect(events, name)


  /** Hyperlink */
  class Link(val text: UString, val href: URL, val textStyle: TextStyle,
      events: MouseEvents, name: UString)
    extends Rect(events, name)


  trait TypesetItem

  /** LoTeX-formatted paragraph. */
  class Paragraph(val text: UString) extends TypesetItem


  /** Typeset item, list. It can contain paragraphs or other lists. */
  class List(val items: Seq[TypesetItem], val style: ListStyle) extends TypesetItem


  /** A Typeset content formats its elements using the given style guide. */
  class Typeset(val content: Seq[TypesetItem], val style: ParagraphStyle,
    events: MouseEvents, name: UString) extends Rect(events, name)


  /** Used to embed an HTML within the content graph */
  class EmbedHTML(val rawHtml: UString, name: UString) extends Content(name)


  /**
   * An image to be loaded from the given relative path.
   */
  class Image(val url: UString, events: MouseEvents, name: UString)
    extends Rect(events, name)


  object TextInput {
    object Semantics extends Enumeration {
      val NONE, USERNAME, PASSWORD = Value
    }
    type Semantics = Semantics.Value
    val DEFAULT_PLACEHOLDER_COLOR: Color = Color.gray(0.2)
  }

  /** One-line text input field. */
  class TextInput(
      val value: UString,
      val isMasked: Boolean,
      val placeholder: Option[UString],
      val textStyle: TextStyle,
      val placeholderColor: Color,
      val semantics: TextInput.Semantics,
      val valueChanges: Flow.Inlet[UString],
      mouseEvents: MouseEvents,
      name: UString)
    extends Rect(mouseEvents, name)


  object Button {
    object Semantics extends Enumeration {
      val NONE, SUBMIT = Value
    }
    type Semantics = Semantics.Value
  }

  /** A clickable button. */
  class Button(val caption: UString,
      val textStyle: TextStyle,
      val semantics: Button.Semantics,
      val pressEvents: Flow.Inlet[Unit],
      mouseEvents: MouseEvents,
      name: UString)
    extends Rect(mouseEvents, name)


  class CheckBox(val checked: Boolean, val caption: Text,
      mouseEvents: MouseEvents, name: UString)
    extends Rect(mouseEvents, name)


  class BandCell(val content: Content, val size: LengthConstraint,
    val vAlign: VAlign)
  {
    override def toString: String = s"BandCell($content)"
  }


  /**
   * A vertical band  places its elements over each other and stretches them
   * based on their length constraints.
   */
  class VerticalBand(val cells: Seq[BandCell], val gap: Length,
      semantics: Container.Semantics, events: MouseEvents, name: UString)
    extends Container(cells.map(_.content), semantics, events, name)


  /**
   * A horizontal band  places its elements horizontally from left to right,
   * and stretches them based on their length constraints.
   */
  class HorizontalBand(val cells: Seq[BandCell], val gap: Length,
      semantics: Container.Semantics, events: MouseEvents, name: UString)
    extends Container(cells.map(_.content), semantics, events, name)


  /**
   * A vertical flow arranges its elements one below another.
   */
  class VerticalFlow(contents: Seq[Content], val hAlign: HAlign,
      semantics: Container.Semantics, events: MouseEvents, name: UString)
    extends Container(contents, semantics, events, name)


  /**
   * A horizontal flow arranges its elements in front of one another.
   */
  class HorizontalFlow(contents: Seq[Content], val align: HAlign,
      semantics: Container.Semantics, events: MouseEvents, name: UString)
    extends Container(contents, semantics, events, name)


  /**
   * Assigns an XY placement to a Content, to be placed inside a Free container.
   */
  class FreePlacement(val x: Length, val y: Length, val item: Content) {
    override def toString: String = s"Free($item)"
  }


  /**
   * A Free container allows its elements to be placed in arbitrary
   * XY coordinates, within its local coordinate system.
   */
  class Free(val items: Seq[FreePlacement],
      semantics: Container.Semantics, events: MouseEvents, name: UString)
    extends Container(items.map(_.item), semantics, events, name)


  /**
   * A Grid container arranges its contents in cells, and then arranges those
   * cells in multiple rows if necessary.
   */
  class Grid(contents: Seq[Content], val columns: LengthConstraintList,
      val rowHeight: LengthConstraint, val columnGap: Length,
      val rowGap: Length, semantics: Container.Semantics,
      events: MouseEvents, name: UString)
    extends Container(contents, semantics, events, name)


  class TableRow(val cells: Seq[Content], val height: LengthConstraint)


  class Table(val cols: Seq[LengthConstraint], val rows: Seq[TableRow],
      semantics: Container.Semantics, events: MouseEvents, name: UString)
    extends Container(rows.flatMap(_.cells), semantics, events, name)


  private val counter = new AtomicInteger


  def autoName(prefix: UString): UString =
    prefix + counter.incrementAndGet()
}