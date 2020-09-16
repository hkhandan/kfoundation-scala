// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse



class CodeLocation(fileName: String, row: Int, col: Int, streamPos: Int)
  extends MutableCodeLocation(fileName, row, col, streamPos)
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
    this(original.getFileName, original.getRow, original.getCol, original.getStreamPos)


  override def step(length: Int): MutableCodeLocation =
    new CodeLocation(fileName, row, col + 1, streamPos + length)


  override def newLine: MutableCodeLocation =
    new CodeLocation(fileName, row+1, 1, streamPos+1)


  override def step(cols: Int, bytes: Int): MutableCodeLocation =
    new CodeLocation(fileName, row, col + cols, streamPos + bytes)

}
