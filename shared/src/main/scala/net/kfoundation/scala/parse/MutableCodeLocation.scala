// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse



/**
 * Represents location of a character in a text file/stream. Row and column
 * numbers start at 1, while streamPos starts at 0. Since the text being read
 * can be UTF-8 or alike, the amount streamPos is incremented per unit column
 * increment can vary each time.
 */
class MutableCodeLocation(
  private val fileName: String,
  private var row: Int,
  private var col: Int,
  private var streamPos: Int)
{

  /**
   * Creates a CodeLocation with row and column set to 1, and streamPos
   * set to 0.
   */
  def this(fileName: String) = this(fileName, 1, 1, 0)


  /**
   * Creates a copy of the given CodeLocation.
   */
  def this(original: MutableCodeLocation) =
    this(original.fileName, original.row, original.col, original.streamPos)


  def getFileName: String = fileName


  def getRow: Int = row


  def getCol: Int = col


  def getStreamPos: Int = streamPos


  def set(other: MutableCodeLocation): MutableCodeLocation = {
    row = other.row
    col = other.col
    streamPos = other.streamPos
    this
  }


  /**
   * Creates a new CodeLocation instance with column being 1 more than this one,
   * adding the given value to streamPos.
   */
  def step(length: Int): MutableCodeLocation = {
    streamPos += length
    col += 1
    this
  }


  /**
   * Produces a new CodeLocation with its column and streamPos increased
   * by the given values.
   */
  def step(cols: Int, bytes: Int): MutableCodeLocation = {
    streamPos += bytes
    col += cols
    this
  }

  /**
   * Creates a new CodeLocation with its row being 1 more than this one and
   * column set to 1. StreamPos is incremented by 1.
   */
  def newLine: MutableCodeLocation = {
    col = 1
    row += 1
    this
  }

  def setStreamPos(pos: Int): MutableCodeLocation = {
    streamPos = pos
    this
  }


  def mutableCopy: MutableCodeLocation = new MutableCodeLocation(this)


  def immutableCopy: CodeLocation = new CodeLocation(this)


  def canEqual(other: Any): Boolean = other.isInstanceOf[MutableCodeLocation]


  def getLocationTag: String = s"[$fileName@$row:$col]"


  /**
   * Creates a CodeRange with this CodeLocation as its start and the given
   * parameter as its end.
   */
  def to(end: MutableCodeLocation) =
    new CodeRange(immutableCopy, end.immutableCopy)


  override def equals(other: Any): Boolean = other match {
    case that: MutableCodeLocation =>
      (that canEqual this) &&
        row == that.row &&
        col == that.col &&
        streamPos == that.streamPos
    case _ => false
  }


  override def toString: String =
    s"CodeLocation($fileName, $row, $col, $streamPos)"

}