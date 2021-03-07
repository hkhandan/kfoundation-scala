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

import java.util.Date



object Chronology {
  class Coordinates(val era: Int, val year: Int, val month: Int,
    val dayOfMoth: Int, val millisOfDay: Int)


  class RichCoordinates(era: Int, year: Int, month: Int, dayOfMonth: Int,
    millisOfDay: Int, calendar: Chronology)
    extends Coordinates(era, year, month, dayOfMonth, millisOfDay)
  {
    def add(other: Coordinates): RichCoordinates = calendar.add(this, other)
    def dayOfWeek: Int = calendar.dayOfWeek(this)
    def weekOfYear: Int = calendar.weekOfYear(this)
    def dayOfYear: Int = calendar.dayOfYear(this)
    def season: Int = calendar.season(this)
    def millisSince(other: Coordinates): Long = calendar.diffInMillis(this, other)
    def daysSince(other: Coordinates): Int = calendar.diffInDays(this, other)
    def weeksSince(other: Coordinates): Int = calendar.diffInWeeks(this, other)
    def monthsSince(other: Coordinates): Int = calendar.diffInMonths(this, other)
    def yearsSince(others: Coordinates): Int = calendar.diffInYears(this, others)
    def nameOfWeekDay: UString = calendar.nameOfWeekDay(dayOfWeek)
    def nameOfMonth: UString = calendar.nameOfMonth(month)
    def nameOfEra: UString = calendar.nameOfEra(era)
    def nameOfSeason: UString = calendar.nameOfSeason(season)
    def toUnixTime: Long = calendar.toUnixTime(this)
    def toDate: Date = new Date(toUnixTime)
  }
}



trait Chronology {
  import Chronology._

  def fromUnixTime(millisFromEpoch: Long): RichCoordinates
  def toUnixTime(c: Coordinates): Long
  def now: RichCoordinates
  def add(from: Coordinates, amount: Coordinates): RichCoordinates
  def diff(to: Coordinates, from: Coordinates): Coordinates
  def diffInMillis(to: Coordinates, from: Coordinates): Long
  def diffInDays(to: Coordinates, from: Coordinates): Int
  def diffInWeeks(to: Coordinates, from: Coordinates): Int
  def diffInMonths(to: Coordinates, from: Coordinates): Int
  def diffInYears(to: Coordinates, from: Coordinates): Int
  def dayOfWeek(c: Coordinates): Int
  def weekOfYear(c: Coordinates): Int
  def dayOfYear(c: Coordinates): Int
  def season(c: Coordinates): Int
  def nameOfWeekDay(day: Int): UString
  def nameOfMonth(month: Int): UString
  def nameOfEra(era: Int): UString
  def nameOfSeason(s: Int): UString
}