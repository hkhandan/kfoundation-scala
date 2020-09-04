package net.kfoundation.scala.i18n

import java.util.Locale

import scala.collection.mutable


object Dialect {
  private val allDialects = new mutable.HashMap[String, Dialect]()

  val EN_US: Dialect = add(new Dialect(Language.EN, Some(Country.USA), None))
  val EN_GB: Dialect = add(new Dialect(Language.EN, Some(Country.GBR), None))

  private def add(d: Dialect): Dialect = {
    allDialects.put(d.getIetfTag, d)
    d
  }

  def of(languageTag: String): Dialect =
    allDialects.getOrElseUpdate(languageTag, {
      val parts = languageTag.split("-")
      if(parts.length < 1) {
        throw new IllegalArgumentException(
          s"Bad language tag format. Should be lang[-region[-script]]. Was: $languageTag")
      }

      val language = Language.of(parts(0))

      val region: Option[Region] =
        if(parts.length > 1) Region.of(parts(1))
        else None

      val script: Option[Script] =
        if(parts.length > 2) Some(new Script(parts.drop(2).mkString("-")))
        else None

      new Dialect(language, region, script)
    })
}


class Dialect(
  val language: Language,
  val region: Option[Region],
  val script: Option[Script])
extends LanguageLike {
  def this(language: Language) = this(language, None, None)
  def this(language: Language, region: Region) = this(language, Some(region), None)

  def withRegion(r: Region) = new Dialect(language, Some(r), script)
  def withScript(s: Script) = new Dialect(language, region, Some(s))

  private def getJavaRegionCode: Option[String] = region.map {
    case c: Country => c.isoAlpha2
    case r => throw new IllegalStateException(
      "Cannot produce Java Locale using a region that is not a country. Given region: " + r)
  }

  private def getRegionCode: Option[String] = region.map(_ match {
    case c: Country => c.isoAlpha2
    case r => r.unm49.toString
  })

  override def asLocale: Locale = getJavaRegionCode match {
    case Some(r) => script match {
      case Some(s) => new Locale(language.isoAlpha2, r, s.code)
      case None => new Locale(language.isoAlpha2, r)
    }
    case None => new Locale(language.isoAlpha2)
  }

  override def asDialect: Dialect = this

  override def getIetfTag: String = language.isoAlpha2 +
    script.map(_.code).getOrElse("") +
    getRegionCode.map("-" + _).getOrElse("")
}
