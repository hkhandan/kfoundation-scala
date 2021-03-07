// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.scala.i18n

import net.kfoundation.scala.UString

import java.util.{Date, TimeZone}



object Localizer {
  implicit class Interpolator(ctx: StringContext)
    extends UString.Interpolator(ctx)
  {
    def l(expr: Any*)(implicit localizer: Localizer): UString =
      localizer.apply(U(expr:_*))
  }
}



trait Localizer {
  def dialect: Dialect
  def apply(key: UString): UString
  def apply(message: LMessage): UString
  def apply(key: UString, values: (UString, UString)*): UString
//  def apply(utc: Date): UString
//  def apply(utc: Date, timezone: TimeZone): UString
//  def apply(number: Long): UString
//  def apply(number: Double): UString
//  def currency(number: Double): UString
}