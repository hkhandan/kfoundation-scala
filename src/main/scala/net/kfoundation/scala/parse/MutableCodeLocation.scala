package net.kfoundation.scala.parse

class MutableCodeLocation(
  private val fileName: String,
  private var row: Int,
  private var col: Int,
  private var streamPos: Int)
{
  def this(fileName: String) = this(fileName, 1, 1, 0)
  def this() = this("buffer")

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

  def step(length: Int): MutableCodeLocation = {
    streamPos += length
    col += 1
    this
  }

  def step(cols: Int, bytes: Int): MutableCodeLocation = {
    streamPos += bytes
    col += cols
    this
  }

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

  override def equals(other: Any): Boolean = other match {
    case that: MutableCodeLocation =>
      (that canEqual this) &&
        row == that.row &&
        col == that.col &&
        streamPos == that.streamPos
    case _ => false
  }

  def getLocationTag: String = s"[$fileName@$row:$col]"

  override def toString: String = s"CodeLocation($fileName, $row, $col, $streamPos)"
}
