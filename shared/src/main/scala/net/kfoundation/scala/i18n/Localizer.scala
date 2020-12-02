package net.kfoundation.scala.i18n

import java.time.format.DateTimeFormatter
import java.util.TimeZone

object Localizer {

  sealed case class DateTimeStyle() {
    object SHORT_DATE extends DateTimeStyle
    object LONG_DATE extends DateTimeStyle
    object SHORT_DATE_TIME extends DateTimeStyle
    object LONG_DATE_TIME extends DateTimeStyle
    object TIME extends DateTimeStyle
  }

  sealed case class NumberStyle() {
    object PLAIN extends NumberStyle
    object CURRENCY extends NumberStyle
  }

  class LocaleSetup(
    val language: LanguageLike,
    val shortDateFormatter: DateTimeFormatter,
    val longDateFormatter: DateTimeFormatter,
    val shortDateTimeFormatter: DateTimeFormatter,
    val longDateTimeFormatter: DateTimeStyle,
    val timeFormatter: DateTimeFormatter,
    val timezone: TimeZone,
    val plainNumberFormatter: NumberStyle,
    val currencyFormatter: NumberStyle
  )

  val DEFAULT = new Localizer(null, null)

  def l(key: String, args: Any*): String = DEFAULT.l(key)
}


class Localizer private(
  private val setup: Localizer.LocaleSetup,
  private val dictionary: Dictionary)
{

    def l(key: String): String = key
//  def l(key: String, language: LanguageLike) = ???
//  def l(utc: Date) = ???
//  def l(utc: Date, format: DateTimeStyle) = ???
//  def l(utc: Date, timezone: TimeZone) = ???
//  def l(utc: Date, timeZone: TimeZone, format: DateTimeStyle) = ???
//  def l(number: Long) = ???
//  def l(number: Double) = ???
//  def l(number: Double, style: NumberStyle) = ???

}
