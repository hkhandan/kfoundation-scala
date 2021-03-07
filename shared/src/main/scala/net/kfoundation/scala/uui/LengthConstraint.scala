// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui



object LengthConstraint {
  /** Constant for no length constant. */
  val NONE = new LengthConstraint(None, None)
}



/**
 * Specifies the minimum and maximum that a length can be. Used for automatic
 * layout adjustments.
 */
class LengthConstraint(val min: Option[Length], val max: Option[Length]) {

  /** True if there are no constraints. */
  def isNone: Boolean = min.isEmpty && max.isEmpty


  /*
   * If min and max constraints are the same, returns their value, otherwise
   * returns None.
   */
  def asExact: Option[Length] = if (min.equals(max)) min else None

  def canEqual(other: Any): Boolean = other.isInstanceOf[LengthConstraint]


  /**
   * Produces a LengthConstraintList made of the specified number of
   * repetitions of this object.
   */
  def x(count: Int): LengthConstraintList =
    new LengthConstraintList(0.until(count).map(_ => this))


  /**
   * Creates a LengthConstraintList made of this object followed by the given
   * parameter.
   */
  def +(other: LengthConstraint): LengthConstraintList =
    new LengthConstraintList(Seq(this, other))


  override def equals(other: Any): Boolean = other match {
    case that: LengthConstraint =>
      (that canEqual this) &&
        min == that.min &&
        max == that.max
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(min, max)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}