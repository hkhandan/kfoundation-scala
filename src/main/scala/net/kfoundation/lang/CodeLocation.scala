package net.kfoundation.lang

class CodeLocation(row: Int, col: Int, streamPos: Int)
  extends MutableCodeLocation(row, col, streamPos)
{
  def this() = this(1, 1, 0)

  def this(original: MutableCodeLocation) =
    this(original.getRow, original.getCol, original.getStreamPos)

  override def step(length: Int): MutableCodeLocation =
    new CodeLocation(row, col + 1, streamPos + length)

  override def newLine: MutableCodeLocation =
    new CodeLocation(row+1, 1, streamPos)

  override def step(cols: Int, length: Int): MutableCodeLocation =
    new CodeLocation(row, col + cols, streamPos + length)
}
