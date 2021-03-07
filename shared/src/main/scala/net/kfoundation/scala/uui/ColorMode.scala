// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui


sealed class ColorMode


/** Color mode that an input/output media can operates on. */
object ColorMode {
  val MONOCHROME = new ColorMode
  val EGA = new ColorMode // 4
  val VGA = new ColorMode // 8
  val XGA = new ColorMode // 16
  val HD = new ColorMode // 24
  val HDR10 = new ColorMode // 30
  val CMYK = new ColorMode // 32
}
