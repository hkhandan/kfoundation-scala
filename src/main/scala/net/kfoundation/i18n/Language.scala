package net.kfoundation.i18n
import java.util.Locale

import scala.collection.mutable


object Language {
  private val allLanguages = new mutable.HashMap[String, Language]()

  val EN: Language = add(new Language("en", "eng"))

  private def add(l: Language): Language = {
    allLanguages.put(l.isoAlpha2, l)
    l
  }

  private def getByIsoAlpha2(code: String): Language = add(new Language(code, code + "x"))

  private def getByIsoAlpha3(code: String): Language = allLanguages.values
    .find(_.isoAlpha3.equals(code))
    .getOrElse(add(new Language(code.substring(0, 2), code)))

  def of(isoAlpha2or3: String): Language = isoAlpha2or3.length match {
    case 2 => getByIsoAlpha2(isoAlpha2or3)
    case 3 => getByIsoAlpha3(isoAlpha2or3)
    case _ => throw new IllegalArgumentException(
      "Parameter should be 2 or 3 letters long. Was: " + isoAlpha2or3)
  }
}


class Language(val isoAlpha2: String, val isoAlpha3: String) extends LanguageLike {
  private val _asDialect = new Dialect(this)

  override def asDialect: Dialect = _asDialect
  override def asLocale: Locale = _asDialect.asLocale
  override def getIetfTag: String = _asDialect.getIetfTag
}