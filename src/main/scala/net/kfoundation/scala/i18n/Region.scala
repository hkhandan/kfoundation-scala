package net.kfoundation.scala.i18n

import scala.collection.mutable

object Region {
  private var allRegions: mutable.HashMap[Int, Region] =
    new mutable.HashMap[Int, Region]()

  val WORLD: Region = add(1, null)
  val AFRICA: Region = add(2, WORLD)
  val AMERICAS: Region = add(19, WORLD)
  val NORTH_AMERICA: Region = add(3, AMERICAS)
  val NORTHERN_AMERICA: Region = add(21, NORTH_AMERICA)
  val ASIA: Region = add(142, WORLD)
  val EUROPE: Region = add(150, WORLD)
  val NORTHERN_EUROPE: Region = add(154, EUROPE)
  val OCEANIA: Region = add(9, WORLD)

  private def add(code: Int, s: Region): Region = {
    val r = new Region(code, s)
    allRegions.put(code, r)
    r
  }

  def of(code: String): Option[Region] = try {
    val c = code.toInt
    Country.of(c).orElse(allRegions.get(c))
  } catch {
    case _: NumberFormatException => Country.of(code)
  }
}

class Region(val unm49: Int, val superRegion: Region)