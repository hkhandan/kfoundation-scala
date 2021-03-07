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

import scala.collection.mutable


object Country {
  private val allCountries = new mutable.HashSet[Country]()

  val GBR: Country = add(new Country(826, "GB", "GBR", Region.NORTHERN_EUROPE))
  val USA: Country = add(new Country(840, "US", "USA", Region.NORTHERN_AMERICA))

  private def add(c: Country): Country = {
    allCountries.add(c)
    c
  }

  def of(code: Int): Option[Country] = allCountries.find(_.unm49 == code)

  def of(code: UString): Option[Country] = try {
    of(code.toInt)
  } catch {
    case _: NumberFormatException => allCountries.find(
      c => c.isoAlpha2.equals(code) || c.isoAlpha3.equals(code))
  }
}


class Country(
  unm49: Int,
  val isoAlpha2: UString,
  val isoAlpha3: UString,
  superRegion: Region)
extends Region(unm49, superRegion)