package net.kfoundation.lang

class CodeRange(
  val file: String,
  val begin: CodeLocation,
  val end: CodeLocation)
{
  def this(original: CodeRange) =
    this(original.file, original.begin, original.end)

  def this(file: String, row: Int, col: Int, len: Int) = this(file,
    new CodeLocation(row, col, col - 1),
    new CodeLocation(row, col + len, col + len - 1))

  def canEqual(other: Any): Boolean = other.isInstanceOf[CodeRange]

  override def equals(other: Any): Boolean = other match {
    case that: CodeRange =>
      (that canEqual this) &&
        begin == that.begin &&
        end == that.end
    case _ => false
  }
}
