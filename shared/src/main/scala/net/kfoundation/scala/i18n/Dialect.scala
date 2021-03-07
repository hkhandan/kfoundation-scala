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
import net.kfoundation.scala.UString.Interpolator
import net.kfoundation.scala.serialization.ValueReadWriter
import net.kfoundation.scala.serialization.ValueReadWriters.STRING

import java.util.Locale
import scala.collection.mutable


object Dialect {
  trait DialectFactory {
    def of(name: UString): Option[Dialect]
  }

  private val allDialects = new mutable.HashMap[UString, Dialect]()

  val EN_US: Dialect = add(new Dialect(Language.EN, Some(Country.USA), None))

  val DEFAULT_FACTORY: DialectFactory =
    (name: UString) => allDialects.get(name)

  def rw(factory: DialectFactory): ValueReadWriter[Dialect] =
    STRING.map(
      toConversion = str => factory.of(str)
        .getOrElse(throw new LException("INVALID_LANGUAGE", U"was"->str)),
      fromConversion = _.getIetfTag)

  private def add(d: Dialect): Dialect = {
    allDialects.put(d.getIetfTag, d)
    d
  }

  def of(languageTag: String): Option[Dialect] =
    allDialects.get(languageTag)
}

/** A dialect is a language, or a variant of a language. */
class Dialect(
    val language: Language,
    val region: Option[Region],
    val script: Option[Script])
  extends LanguageLike
{
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

  override def getIetfTag: UString = language.isoAlpha2 +
    script.map("-" + _.code).getOrElse("") +
    getRegionCode.map("-" + _).getOrElse("")

  override def toString: String = getIetfTag.toString
}
