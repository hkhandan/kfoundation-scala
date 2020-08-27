package net.kfoundation.i18n

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

  def of(code: String): Option[Country] = try {
    of(code.toInt)
  } catch {
    case _: NumberFormatException => allCountries.find(
      c => c.isoAlpha2.equals(code) || c.isoAlpha3.equals(code))
  }
}


class Country(
  unm49: Int,
  val isoAlpha2: String,
  val isoAlpha3: String,
  superRegion: Region)
extends Region(unm49, superRegion)
