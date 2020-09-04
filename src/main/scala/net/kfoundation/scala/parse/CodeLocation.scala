package net.kfoundation.scala.parse

class CodeLocation(fileName: String, row: Int, col: Int, streamPos: Int)
  extends MutableCodeLocation(fileName, row, col, streamPos)
{
  def this(fileName: String) = this(fileName, 1, 1, 0)
  def this() = this("buffer")

  def this(original: MutableCodeLocation) =
    this(original.getFileName, original.getRow, original.getCol, original.getStreamPos)

  override def step(length: Int): MutableCodeLocation =
    new CodeLocation(fileName, row, col + 1, streamPos + length)

  override def newLine: MutableCodeLocation =
    new CodeLocation(fileName, row+1, 1, streamPos)

  override def step(cols: Int, bytes: Int): MutableCodeLocation =
    new CodeLocation(fileName, row, col + cols, streamPos + bytes)
}
