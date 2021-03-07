// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.uui


object ListStyle {

  class Bulleted extends ListStyle

  val DISK = new Bulleted
  val CIRCLE = new Bulleted
  val SQUARE = new Bulleted

  class Numbered extends ListStyle

  val ARMENIAN = new Numbered
  val DECIMAL = new Numbered
  val DECIMAL_LEADING_ZERO = new Numbered
  val GEORGIAN = new Numbered
  val HEBREW = new Numbered
  val HIRAGANA = new Numbered
  val HIRAGANA_IROHA = new Numbered
  val KATAKANA = new Numbered
  val KATAKANA_IROHA = new Numbered
  val LOWER_ALPHA = new Numbered
  val LOWER_GREEK = new Numbered
  val LOWER_LATIN = new Numbered
  val LOWER_ROMAN = new Numbered
  val UPPER_ALPHA = new Numbered
  val UPPER_LATIN = new Numbered
  val UPPER_ROMAN = new Numbered
}

trait ListStyle
