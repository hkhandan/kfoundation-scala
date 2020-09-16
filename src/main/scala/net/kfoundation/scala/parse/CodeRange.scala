// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.parse

import net.kfoundation.scala.UString


object CodeRange {

  /** Creates a CodeRange that starts and ends at the same location */
  def of(location: CodeLocation): CodeRange = {
    new CodeRange(location, location)
  }


  def of(fileName: String, row: Int, startCol: Int, nCols: Int, startPos: Int, len: Int) =
    new CodeRange(
      new CodeLocation(fileName, row, startCol, startPos),
      new CodeLocation(fileName, row, startCol + nCols, startPos + len))

}



/**
 * A CodeRange consists a beginning and an end represented by two CodeLocation
 * objects.
 */
class CodeRange(
  val begin: CodeLocation,
  val end: CodeLocation)
{

  /** Copy constructor. */
  def this(original: CodeRange) =
    this(original.begin, original.end)


  def canEqual(other: Any): Boolean = other.isInstanceOf[CodeRange]


  def file: String = begin.getFileName


  override def equals(other: Any): Boolean = other match {
    case that: CodeRange =>
      (that canEqual this) &&
        begin == that.begin &&
        end == that.end
    case _ => false
  }
}
