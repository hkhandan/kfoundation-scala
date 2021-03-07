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

import java.util.Locale
import scala.collection.mutable


object Language {
  private val allLanguages = new mutable.HashMap[UString, Language]()

  val EN: Language = add(new Language("en", "eng"))

  private def add(l: Language): Language = {
    allLanguages.put(l.isoAlpha2, l)
    l
  }

  def getByIsoAlpha2(code: UString): Option[Language] = allLanguages.values
    .find(_.isoAlpha2.equals(code))

  def getByIsoAlpha3(code: UString): Option[Language] = allLanguages.values
    .find(_.isoAlpha3.equals(code))

  def of(isoAlpha2or3: UString): Option[Language] = isoAlpha2or3.length match {
    case 2 => getByIsoAlpha2(isoAlpha2or3)
    case 3 => getByIsoAlpha3(isoAlpha2or3)
    case _ => throw new IllegalArgumentException(
      "Parameter should be 2 or 3 letters long. Was: " + isoAlpha2or3)
  }
}


class Language(val isoAlpha2: UString, val isoAlpha3: UString) extends LanguageLike {
  private val _asDialect = new Dialect(this)

  override def asDialect: Dialect = _asDialect
  override def asLocale: Locale = _asDialect.asLocale
  override def getIetfTag: UString = _asDialect.getIetfTag

  override def toString: String = isoAlpha3.toString

  override def equals(other: Any): Boolean = other match {
    case that: Language =>
        isoAlpha3 == that.isoAlpha3
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(isoAlpha3)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}